package com.namdang.memos.service.impl;

import com.namdang.memos.config.OryConfig;
import com.namdang.memos.dto.responses.auth.RegistrationResult;
import com.namdang.memos.dto.responses.auth.TokenPair;
import com.namdang.memos.dto.responses.ory.OryResponse;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.Permission;
import com.namdang.memos.entity.Role;
import com.namdang.memos.enumType.AuthProvider;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.RoleRepository;
import com.namdang.memos.service.AuthenticationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OryAuthServiceImplTest {

    @Mock OryConfig oryConfig;
    @Mock RestTemplate restTemplate;
    @Mock AccountRepository accountRepository;
    @Mock RoleRepository roleRepository;
    @Mock AuthenticationService authenticationService;

    @InjectMocks OryAuthServiceImpl oryAuthService;

    private static OryResponse session(String identityId, String email, boolean active, String oidcProvider) {
        OryResponse s = new OryResponse();
        s.setActive(active);

        OryResponse.Identity id = new OryResponse.Identity();
        id.setId(identityId);

        OryResponse.Traits traits = new OryResponse.Traits();
        traits.setEmail(email);
        id.setTraits(traits);

        s.setIdentity(id);

        if (oidcProvider != null) {
            OryResponse.AuthenticationMethod am = new OryResponse.AuthenticationMethod();
            am.setMethod("oidc");
            am.setProvider(oidcProvider);
            s.setAuthenticationMethods(List.of(am));
        } else {
            s.setAuthenticationMethods(Collections.emptyList());
        }

        return s;
    }

    private static Role roleWithPerms(String roleName, String... permNames) {
        Role r = new Role();
        r.setName(roleName);
        Set<Permission> perms = new HashSet<>();
        for (String p : permNames) {
            Permission perm = new Permission();
            perm.setName(p);
            perms.add(perm);
        }
        r.setPermissions(perms);
        return r;
    }

    private static TokenPair tokenPair() {
        return TokenPair.builder()
                .accessToken("access.xxx")
                .refreshToken("refresh.yyy")
                .build();
    }

    @Test
    void loginFromOrySession_missingCookie_throwsMissingOryCookies() {
        assertThatThrownBy(() -> oryAuthService.loginFromOrySession(null, "v"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.MISSING_ORY_COOKIES);

        assertThatThrownBy(() -> oryAuthService.loginFromOrySession("ory_kratos_session", "  "))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.MISSING_ORY_COOKIES);

        verifyNoInteractions(restTemplate, accountRepository, roleRepository, authenticationService);
    }

    @Test
    void loginFromOrySession_invalidSession_throwsInvalidOryCookies() {
        OryAuthServiceImpl spy = Mockito.spy(oryAuthService);

        // body null
        doReturn(ResponseEntity.ok(null))
                .when(spy).callWhoAmI(anyString(), anyString());

        assertThatThrownBy(() -> spy.loginFromOrySession("c", "v"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ORY_COOKIES);

        // inactive
        doReturn(ResponseEntity.ok(session("id1", "a@test.com", false, "google")))
                .when(spy).callWhoAmI(anyString(), anyString());

        assertThatThrownBy(() -> spy.loginFromOrySession("c", "v"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_ORY_COOKIES);

        verifyNoInteractions(accountRepository, roleRepository, authenticationService);
    }


    @Test
    void loginFromOrySession_existingByIdentity_updatesEmailAndProviderIfChanged_andReturnsTokens() {
        String cookieName = "ory_kratos_session";
        String cookieValue = "cookie";
        String identityId = "ory-id-1";
        String emailFromOry = "new@email.com";

        OryResponse s = session(identityId, emailFromOry, true, "github");

        OryAuthServiceImpl spy = Mockito.spy(oryAuthService);
        doReturn(ResponseEntity.ok(s)).when(spy).callWhoAmI(cookieName, cookieValue);

        Role member = roleWithPerms("MEMBER", "PROJECT.CREATE", "PROJECT.READ");
        Account existing = new Account();
        existing.setId(UUID.randomUUID());
        existing.setEmail("old@email.com");
        existing.setOryIdentityId(identityId);
        existing.setProvider(AuthProvider.GOOGLE);
        existing.setActive(true);
        existing.setRoles(new HashSet<>(Set.of(member)));

        when(accountRepository.findByOryIdentityId(identityId)).thenReturn(Optional.of(existing));

        // IMPORTANT: make save return the same entity (not null)
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        // IMPORTANT: stub with any(Account.class) to avoid strict mismatch
        when(authenticationService.generateTokenPair(any(Account.class))).thenReturn(tokenPair());

        RegistrationResult result = spy.loginFromOrySession(cookieName, cookieValue);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();

        assertThat(saved.getEmail()).isEqualTo(emailFromOry);
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.GITHUB);
        assertThat(saved.getOryIdentityId()).isEqualTo(identityId);

        assertThat(result.getTokenPair().getAccessToken()).isEqualTo("access.xxx");
        assertThat(result.getRegisterResponse().getProvider()).isEqualTo(AuthProvider.GITHUB);
        assertThat(result.getRegisterResponse().getRole()).isEqualTo("MEMBER");
        assertThat(result.getRegisterResponse().getPermissions()).contains("PROJECT.CREATE", "PROJECT.READ");
    }


    @Test
    void loginFromOrySession_existingByIdentity_noChanges_doesNotSave_andReturnsTokens() {
        String cookieName = "ory_kratos_session";
        String cookieValue = "cookie";
        String identityId = "ory-id-1";
        String emailFromOry = "same@email.com";

        OryResponse s = session(identityId, emailFromOry, true, "google");

        OryAuthServiceImpl spy = Mockito.spy(oryAuthService);
        doReturn(ResponseEntity.ok(s)).when(spy).callWhoAmI(cookieName, cookieValue);

        Role member = roleWithPerms("MEMBER", "PROJECT.CREATE");
        Account existing = new Account();
        existing.setId(UUID.randomUUID());
        existing.setEmail(emailFromOry);
        existing.setOryIdentityId(identityId);
        existing.setProvider(AuthProvider.GOOGLE);
        existing.setActive(true);
        existing.setRoles(new HashSet<>(Set.of(member)));

        when(accountRepository.findByOryIdentityId(identityId)).thenReturn(Optional.of(existing));
        when(authenticationService.generateTokenPair(existing)).thenReturn(tokenPair());

        RegistrationResult result = spy.loginFromOrySession(cookieName, cookieValue);

        verify(accountRepository, never()).save(any());
        assertThat(result.getRegisterResponse().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.getRegisterResponse().getPermissions()).contains("PROJECT.CREATE");
    }

    @Test
    void loginFromOrySession_noIdentityMatch_butEmailExists_syncsAccount_setsOryIdentity_andProviderLocalOidc() {
        String cookieName = "ory_kratos_session";
        String cookieValue = "cookie";
        String identityId = "ory-id-2";
        String email = "member@test.com";

        // provider from ory: github, but since existingByEmail has LOCAL -> becomes LOCAL_OIDC
        OryResponse s = session(identityId, email, true, "github");

        OryAuthServiceImpl spy = Mockito.spy(oryAuthService);
        doReturn(ResponseEntity.ok(s)).when(spy).callWhoAmI(cookieName, cookieValue);

        Role member = roleWithPerms("MEMBER", "PROJECT.CREATE");
        Account existingByEmail = new Account();
        existingByEmail.setId(UUID.randomUUID());
        existingByEmail.setEmail(email);
        existingByEmail.setProvider(AuthProvider.LOCAL);
        existingByEmail.setActive(true);
        existingByEmail.setRoles(new HashSet<>(Set.of(member)));

        when(accountRepository.findByOryIdentityId(identityId)).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(existingByEmail));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authenticationService.generateTokenPair(existingByEmail)).thenReturn(tokenPair());

        RegistrationResult result = spy.loginFromOrySession(cookieName, cookieValue);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();

        assertThat(saved.getOryIdentityId()).isEqualTo(identityId);
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL_OIDC); // per logic
        assertThat(result.getRegisterResponse().getProvider()).isEqualTo(AuthProvider.LOCAL_OIDC);
    }

    @Test
    void loginFromOrySession_noIdentityMatch_butEmailExists_withNonLocalProvider_setsProviderToResolvedProvider() {
        String cookieName = "ory_kratos_session";
        String cookieValue = "cookie";
        String identityId = "ory-id-3";
        String email = "member@test.com";

        OryResponse s = session(identityId, email, true, "facebook");

        OryAuthServiceImpl spy = Mockito.spy(oryAuthService);
        doReturn(ResponseEntity.ok(s)).when(spy).callWhoAmI(cookieName, cookieValue);

        Role member = roleWithPerms("MEMBER", "PROJECT.CREATE");
        Account existingByEmail = new Account();
        existingByEmail.setId(UUID.randomUUID());
        existingByEmail.setEmail(email);
        existingByEmail.setProvider(AuthProvider.GOOGLE); // non-local
        existingByEmail.setActive(true);
        existingByEmail.setRoles(new HashSet<>(Set.of(member)));

        when(accountRepository.findByOryIdentityId(identityId)).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(existingByEmail));
        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));
        when(authenticationService.generateTokenPair(existingByEmail)).thenReturn(tokenPair());

        RegistrationResult result = spy.loginFromOrySession(cookieName, cookieValue);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account saved = captor.getValue();

        assertThat(saved.getProvider()).isEqualTo(AuthProvider.FACEBOOK);
        assertThat(result.getRegisterResponse().getProvider()).isEqualTo(AuthProvider.FACEBOOK);
    }

    @Test
    void loginFromOrySession_newUser_createsAccount_setsMemberRole_andProviderFromOry() {
        String cookieName = "ory_kratos_session";
        String cookieValue = "cookie";
        String identityId = "ory-id-4";
        String email = "new@test.com";

        OryResponse s = session(identityId, email, true, "google");

        OryAuthServiceImpl spy = Mockito.spy(oryAuthService);
        doReturn(ResponseEntity.ok(s)).when(spy).callWhoAmI(cookieName, cookieValue);

        when(accountRepository.findByOryIdentityId(identityId)).thenReturn(Optional.empty());
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        Role memberRole = roleWithPerms("MEMBER", "PROJECT.CREATE", "PROJECT.READ");
        when(roleRepository.findByName("MEMBER")).thenReturn(memberRole);

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        // generateTokenPair called with the saved user instance
        when(authenticationService.generateTokenPair(any(Account.class))).thenReturn(tokenPair());

        RegistrationResult result = spy.loginFromOrySession(cookieName, cookieValue);

        ArgumentCaptor<Account> captor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(captor.capture());
        Account created = captor.getValue();

        assertThat(created.getEmail()).isEqualTo(email);
        assertThat(created.getOryIdentityId()).isEqualTo(identityId);
        assertThat(created.getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(created.isActive()).isTrue();
        assertThat(created.getRoles()).isNotEmpty();
        assertThat(created.getRoles().iterator().next().getName()).isEqualTo("MEMBER");

        assertThat(result.getRegisterResponse().getProvider()).isEqualTo(AuthProvider.GOOGLE);
        assertThat(result.getRegisterResponse().getRole()).isEqualTo("MEMBER");
        assertThat(result.getRegisterResponse().getPermissions())
                .contains("PROJECT.CREATE", "PROJECT.READ");
    }
}
