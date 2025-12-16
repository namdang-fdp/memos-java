package com.namdang.memos.service;

import com.namdang.memos.dto.requests.auth.AccountSetupRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.responses.auth.*;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.InvalidatedToken;
import com.namdang.memos.entity.Role;
import com.namdang.memos.enumType.AuthProvider;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.mapper.account.ProfileMapper;
import com.namdang.memos.mapper.auth.RegisterMapper;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.InvalidatedTokenRepository;
import com.namdang.memos.repository.RoleRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock PasswordEncoder passwordEncoder;
    @Mock AccountRepository accountRepository;
    @Mock InvalidatedTokenRepository invalidatedTokenRepository;
    @Mock RoleRepository roleRepository;
    @Mock RegisterMapper registerMapper;
    @Mock ProfileMapper profileMapper;

    @InjectMocks AuthenticationService authenticationService;

    private String signerKeyBase64;
    private long validDuration;
    private long refreshableDuration;

    @BeforeEach
    void setUp() {
        signerKeyBase64 = "XT/Y//x7C1ohLLN2cUdVMLjXPDsLcWFLZvMCEJ9JC7Z8mHU83QxdJ8hYqyTZdknlVJPtujJP7IpTneFyJUDGVA==";
        validDuration = 3600L;
        refreshableDuration = 86400L;

        ReflectionTestUtils.setField(authenticationService, "SIGNER_KEY", signerKeyBase64);
        ReflectionTestUtils.setField(authenticationService, "VALID_DURATION", validDuration);
        ReflectionTestUtils.setField(authenticationService, "REFRESHABLE_DURATION", refreshableDuration);
    }

    private String signedToken(String email, String jti, Date issueTime, Date expTime, boolean isRefresh) {
        try {
            JWTClaimsSet.Builder b = new JWTClaimsSet.Builder()
                    .subject(email)
                    .issuer("namdang-fdp")
                    .issueTime(issueTime)
                    .expirationTime(expTime)
                    .jwtID(jti)
                    .claim("scope", "ROLE_MEMBER");

            if (isRefresh) b.claim("tok", "REFRESH");

            JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
            JWSObject jwsObject = new JWSObject(header, new Payload(b.build().toJSONObject()));
            jwsObject.sign(new MACSigner(signerKeyBase64));

            return jwsObject.serialize();
        } catch (JOSEException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void refreshFromCookie_blank_throwsMissingRefreshCookie() {
        assertThatThrownBy(() -> authenticationService.refreshFromCookie("  "))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.MISSING_REFRESH_TOKEN_COOKIE);

        verifyNoInteractions(accountRepository);
        verify(invalidatedTokenRepository, never()).save(any());
    }

    @Test
    void refreshFromCookie_success_invalidatesOldRefresh_andReturnsNewPair() throws Exception {
        String email = "member@test.com";

        Date iat = Date.from(Instant.now().minus(5, ChronoUnit.MINUTES));
        Date exp = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        String oldJti = UUID.randomUUID().toString();

        String refreshToken = signedToken(email, oldJti, iat, exp, true);

        Role role = new Role();
        role.setName("MEMBER");
        role.setPermissions(Collections.emptySet());

        Account acc = new Account();
        acc.setEmail(email);
        acc.setPassword("hashed");
        acc.setRoles(Set.of(role));

        when(invalidatedTokenRepository.existsById(oldJti)).thenReturn(false);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(acc));

        LoginResult result = authenticationService.refreshFromCookie(refreshToken);

        assertThat(result.getTokenPair().getAccessToken()).isNotBlank();
        assertThat(result.getTokenPair().getRefreshToken()).isNotBlank();
        assertThat(result.getRole()).isEqualTo("MEMBER");

        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenRepository).save(captor.capture());

        assertThat(captor.getValue().getId()).isEqualTo(oldJti);
        // one second diff is valid
        assertThat(captor.getValue().getExpiryTime())
                .isCloseTo(exp, 1000L);

    }

    @Test
    void logout_withAccessAndRefresh_savesTwoInvalidations() throws Exception {
        String email = "member@test.com";

        Date accessIat = new Date();
        Date accessExp = Date.from(Instant.now().plus(30, ChronoUnit.MINUTES));
        String accessJti = UUID.randomUUID().toString();
        String access = signedToken(email, accessJti, accessIat, accessExp, false);

        Date refreshIat = Date.from(Instant.now().minus(1, ChronoUnit.MINUTES));
        Date refreshExp = Date.from(Instant.now().plus(30, ChronoUnit.DAYS));
        String refreshJti = UUID.randomUUID().toString();
        String refresh = signedToken(email, refreshJti, refreshIat, refreshExp, true);

        when(invalidatedTokenRepository.existsById(accessJti)).thenReturn(false);
        when(invalidatedTokenRepository.existsById(refreshJti)).thenReturn(false);

        authenticationService.logout("Bearer " + access, refresh);

        ArgumentCaptor<InvalidatedToken> captor = ArgumentCaptor.forClass(InvalidatedToken.class);
        verify(invalidatedTokenRepository, times(2)).save(captor.capture());

        List<InvalidatedToken> saved = captor.getAllValues();
        assertThat(saved).extracting(InvalidatedToken::getId)
                .containsExactlyInAnyOrder(accessJti, refreshJti);
    }

    @Test
    void register_emailExists_throwsEmailAlreadyExist() {
        AuthenticationRequest req = AuthenticationRequest.builder()
                .email("dup@test.com")
                .password("123")
                .build();

        when(accountRepository.existsByEmail("dup@test.com")).thenReturn(true);

        assertThatThrownBy(() -> authenticationService.register(req))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.EMAIL_ALREADY_EXIST);

        verify(accountRepository, never()).save(any());
    }

    @Test
    void register_success_savesAccount_andReturnsTokenPair() {
        AuthenticationRequest req = AuthenticationRequest.builder()
                .email("new@test.com")
                .password("123")
                .build();

        Role member = new Role();
        member.setName("MEMBER");
        member.setPermissions(Collections.emptySet());

        when(accountRepository.existsByEmail("new@test.com")).thenReturn(false);
        when(roleRepository.findByName("MEMBER")).thenReturn(member);
        when(passwordEncoder.encode("123")).thenReturn("hashed");

        when(accountRepository.save(any(Account.class))).thenAnswer(inv -> inv.getArgument(0));

        RegisterResponse dummyRegisterResponse = mock(RegisterResponse.class);
        when(registerMapper.toRegisterResponse(any(Account.class), anyString())).thenReturn(dummyRegisterResponse);

        RegistrationResult result = authenticationService.register(req);

        assertThat(result.getTokenPair().getAccessToken()).isNotBlank();
        assertThat(result.getTokenPair().getRefreshToken()).isNotBlank();
        assertThat(result.getRegisterResponse()).isNotNull();

        ArgumentCaptor<Account> accCaptor = ArgumentCaptor.forClass(Account.class);
        verify(accountRepository).save(accCaptor.capture());
        Account saved = accCaptor.getValue();

        assertThat(saved.getEmail()).isEqualTo("new@test.com");
        assertThat(saved.getPassword()).isEqualTo("hashed");
        assertThat(saved.isActive()).isTrue();
        assertThat(saved.getProvider()).isEqualTo(AuthProvider.LOCAL);
    }

    @Test
    void me_missingAuthHeader_throwsMissingAuthHeader() {
        assertThatThrownBy(() -> authenticationService.me(null))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.MISSING_AUTH_HEADER);

        assertThatThrownBy(() -> authenticationService.me("Token abc"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.MISSING_AUTH_HEADER);
    }

    @Test
    void me_success_returnsProfileFromMapper() throws Exception {
        String email = "member@test.com";

        Date iat = new Date();
        Date exp = Date.from(Instant.now().plus(10, ChronoUnit.MINUTES));
        String jti = UUID.randomUUID().toString();
        String access = signedToken(email, jti, iat, exp, false);

        Role role = new Role();
        role.setName("MEMBER");
        role.setPermissions(Collections.emptySet());

        Account acc = new Account();
        acc.setEmail(email);
        acc.setRoles(Set.of(role));

        when(invalidatedTokenRepository.existsById(jti)).thenReturn(false);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(acc));

        when(profileMapper.getPrimaryRole(acc)).thenReturn("MEMBER");
        when(profileMapper.getPermissionNames(acc)).thenReturn(Set.of("TASK.READ"));

        MeResponse mapped = mock(MeResponse.class);
        when(profileMapper.toProfile(acc, "MEMBER", Set.of("TASK.READ"))).thenReturn(mapped);

        MeResponse result = authenticationService.me("Bearer " + access);

        assertThat(result).isSameAs(mapped);
    }


    @Test
    void profileSetup_invalidEmail_throwsInvalidEmail() {
        when(accountRepository.findByEmail("x@test.com")).thenReturn(Optional.empty());

        AccountSetupRequest req = AccountSetupRequest.builder().name("New Name").build();

        assertThatThrownBy(() -> authenticationService.profileSetup("x@test.com", req))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);
    }

    @Test
    void profileSetup_success_setsName_andReturnsProfile() {
        String email = "member@test.com";

        Role role = new Role();
        role.setName("MEMBER");
        role.setPermissions(Collections.emptySet());

        Account acc = new Account();
        acc.setEmail(email);
        acc.setName("Old");
        acc.setRoles(Set.of(role));

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(acc));

        when(profileMapper.getPrimaryRole(acc)).thenReturn("MEMBER");
        when(profileMapper.getPermissionNames(acc)).thenReturn(Set.of("TASK.READ"));

        MeResponse mapped = mock(MeResponse.class);
        when(profileMapper.toProfile(acc, "MEMBER", Set.of("TASK.READ"))).thenReturn(mapped);

        AccountSetupRequest req = AccountSetupRequest.builder().name("New Name").build();

        MeResponse result = authenticationService.profileSetup(email, req);

        assertThat(acc.getName()).isEqualTo("New Name");
        assertThat(result).isSameAs(mapped);
    }
}
