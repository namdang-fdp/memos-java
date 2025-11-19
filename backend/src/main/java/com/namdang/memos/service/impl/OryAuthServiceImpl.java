package com.namdang.memos.service.impl;

import com.namdang.memos.config.OryConfig;
import com.namdang.memos.dto.responses.auth.RegisterResponse;
import com.namdang.memos.dto.responses.auth.RegistrationResult;
import com.namdang.memos.dto.responses.auth.TokenPair;
import com.namdang.memos.dto.responses.ory.OryResponse;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.Role;
import com.namdang.memos.enumType.AuthProvider;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.RoleRepository;
import com.namdang.memos.service.AuthenticationService;
import com.namdang.memos.service.OryAuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
// Flow here:
// After FE complete their flow to get cookies (ory_kratos_cookies)
// They will send that cookies to backend, backend link with ory to read it
public class OryAuthServiceImpl implements OryAuthService {
    private final OryConfig oryConfig;
    private final RestTemplate restTemplate;
    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final AuthenticationService authenticationService;

    // function to read user session info base on ory cookies
    @Override
    public ResponseEntity<OryResponse> callWhoAmI(String cookieName, String cookieValue) {
        String url = oryConfig.getPublicUrl() + "/sessions/whoami";
        String cookie = cookieName + "=" + cookieValue;
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookie);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, OryResponse.class);
    }

    // After get session info, create or read user
    // This is where the business logic occurs
    @Override
    @Transactional
    public RegistrationResult loginFromOrySession(String cookieName, String cookieValue) {
        if(cookieValue == null || cookieValue.isBlank() || cookieName == null || cookieName.isBlank()) {
            throw new AppException(ErrorCode.MISSING_ORY_COOKIES);
        }
        ResponseEntity<OryResponse> response = callWhoAmI(cookieName, cookieValue);

        OryResponse session = response.getBody();
        if (session == null || !session.isActive() || session.getIdentity() == null
                || session.getIdentity().getTraits() == null
                || session.getIdentity().getTraits().getEmail() == null) {
            throw new AppException(ErrorCode.INVALID_ORY_COOKIES);
        }
        System.out.println(session);

        String identityId = session.getIdentity().getId();
        String email = session.getIdentity().getTraits().getEmail();
        AuthProvider provider = resolveProvider(session);

        // in my application, when some OIDC login request occur, I try to look
        // for the oryIdentity 1st, since it unique, after that is look for mail
        Account user = accountRepository.findByOryIdentityId(identityId).orElse(null);

        // this user already login by OIDC before
        // if last time they login by facebook --> provider = facebook
        // now they login by github --> change provider to github but keep all of their info
        if(user != null) {
            boolean isChange = false;
            if(!email.equalsIgnoreCase(user.getEmail())) {
                user.setEmail(email);
                isChange = true;
            }
            if(user.getProvider() == null || user.getProvider() != provider) {
                user.setProvider(provider);
                isChange = true;
            }
            if (user.getOryIdentityId() == null) {
                user.setOryIdentityId(identityId);
                isChange = true;
            }
            if (isChange) {
                user = accountRepository.save(user);
            }
        } else {
            // in this branch, there is a logic if this is the first time user login
            // there are 2 things we have to deal with
            // 1. User 1st time access the system and they choice OIDC method
            // 2. User already access the system by email + password and this is the 1st time they access by OIDC
            // We find by email, if it exist --> case 2. If not --> case 1
            Account existingByEmail = accountRepository.findByEmail(email).orElse(null);
            // if the user login by both traditional and OIDC, this is the logic to sync 2 account
            // case 2 process here
            if(existingByEmail != null) {
                boolean isChange = false;
                if(existingByEmail.getOryIdentityId() == null) {
                    existingByEmail.setOryIdentityId(identityId);
                    isChange = true;
                }
                if(existingByEmail.getProvider() == null || existingByEmail.getProvider() == AuthProvider.LOCAL) {
                    existingByEmail.setProvider(AuthProvider.LOCAL_OIDC);
                    isChange = true;
                } else {
                    existingByEmail.setProvider(provider);
                    isChange = true;
                }
                if(isChange) {
                    user = accountRepository.save(existingByEmail);
                } else {
                    user = existingByEmail;
                }

            } else {
                // case 1 here: Just create new account for user
                Account newUser = new Account();
                newUser.setEmail(email);
                newUser.setOryIdentityId(identityId);
                newUser.setProvider(provider);
                newUser.setActive(true);
                Role memberRole = roleRepository.findByName("MEMBER");
                newUser.getRoles().add(memberRole);
                user = accountRepository.save(newUser);
            }
        }
        TokenPair tokenPair = authenticationService.generateTokenPair(user);

        user.getRoles().forEach(r -> r.getPermissions().size());

        String roleName = user.getRoles().iterator().next().getName();

        Set<String> permissionNames = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getName())
                .collect(Collectors.toSet());
        RegisterResponse registerResponse = RegisterResponse.builder()
                .accessToken(tokenPair.getAccessToken())
                .role(roleName)
                .permissions(permissionNames)
                .provider(user.getProvider())
                .build();
        return RegistrationResult.builder()
                .registerResponse(registerResponse)
                .tokenPair(tokenPair)
                .build();
    }

    // helpers to map provider of my app (my business logic)
    // if user login by email + password --> local
    // if user login by ory, map base on oidc. ex: facebook --> FACEBOOK
    // if that user's mail login both email password and oidc --> LOCAL_OIDC
    private AuthProvider resolveProvider(OryResponse session) {
        if (CollectionUtils.isEmpty(session.getAuthenticationMethods())) {
            return AuthProvider.LOCAL_OIDC;
        }

        return session.getAuthenticationMethods().stream()
                .filter(am -> "oidc".equalsIgnoreCase(am.getMethod()))
                .findFirst()
                .map(am -> mapProvider(am.getProvider()))
                .orElse(AuthProvider.LOCAL_OIDC);
    }

    private AuthProvider mapProvider(String provider) {
        if (provider == null) return AuthProvider.LOCAL_OIDC;
        String p = provider.toLowerCase(Locale.ROOT).trim();
        if (p.startsWith("google")) return AuthProvider.GOOGLE;
        if (p.startsWith("facebook")) return AuthProvider.FACEBOOK;
        if (p.startsWith("github")) return AuthProvider.GITHUB;
        return AuthProvider.LOCAL_OIDC;
    }


}
