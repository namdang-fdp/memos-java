package com.namdang.memos.service;

import com.namdang.memos.dto.requests.LogoutRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.requests.auth.IntrospectRequest;
import com.namdang.memos.dto.responses.auth.*;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.InvalidatedToken;
import com.namdang.memos.entity.Role;
import com.namdang.memos.enumType.AuthProvider;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.mapper.RegisterMapper;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.InvalidatedTokenRepository;
import com.namdang.memos.repository.RoleRepository;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    private final PasswordEncoder passwordEncoder;
    private final AccountRepository accountRepository;
    private final InvalidatedTokenRepository invalidatedTokenRepository;
    private final RoleRepository roleRepository;
    private final RegisterMapper registerMapper;

    @NonFinal
    @Value("${JWT_SIGNER_KEY_BASE64}")
    protected String SIGNER_KEY;

    @NonFinal
    @Value("${JWT_VALID_DURATION}")
    protected Long VALID_DURATION;

    @NonFinal
    @Value("${JWT_REFRESHABLE_DURATION}")
    protected Long REFRESHABLE_DURATION;

    // implement 1st --> return string role + permission for deep authorization
    private String buildScope(Account account) {
        StringJoiner stringJoiner = new StringJoiner(" ");
        if(!CollectionUtils.isEmpty(account.getRoles())) {
            account.getRoles().forEach(role -> {
                stringJoiner.add("ROLE_" + role.getName());
                if(!CollectionUtils.isEmpty(role.getPermissions())) {
                    role.getPermissions().forEach(permission -> {
                        stringJoiner.add(permission.getName());
                    });
                }
            });
        }
        return stringJoiner.toString();
    }

    // implement 2nd --> generate a new valid jwt token
    // JWT: header.payload.signature
    // Algo: HS512, Type: Symmetric key --> this means there are one key use for both sign and verify token
    // By the way, Asymmetric key is better but more complicated
    private String generateToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512); // this is header
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder() // this is payload
                .subject(account.getEmail()) // owner of token info
                .issuer("namdang-fdp") // person who grant this token
                .issueTime(new Date()) // granted at time
                .expirationTime(new Date(
                        Instant.now().plus(VALID_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                )) // expiration date of this token
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account)) // authorized work here
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY)); // signed
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create token: Reason ", e);
            throw new RuntimeException(e);
        }
    }

    // generate refresh token. the same logic with other refreshable time
    private String generateRefreshToken(Account account) {
        JWSHeader header = new JWSHeader(JWSAlgorithm.HS512);
        JWTClaimsSet jwtClaimsSet = new JWTClaimsSet.Builder()
                .subject(account.getEmail())
                .issuer("namdang-fdp")
                .issueTime(new Date())
                .expirationTime(new Date(Instant.now().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli()
                ))
                .jwtID(UUID.randomUUID().toString())
                .claim("scope", buildScope(account))
                .claim("tok", "REFRESH")
                .build();
        Payload payload = new Payload(jwtClaimsSet.toJSONObject());
        JWSObject jwsObject = new JWSObject(header, payload);
        try {
            jwsObject.sign(new MACSigner(SIGNER_KEY)); // signed
            return jwsObject.serialize();
        } catch (JOSEException e) {
            log.error("Cannot create refresh token: Reason ", e);
            throw new RuntimeException(e);
        }
    }

    // generate a pair of access and refresh token
    public TokenPair generateTokenPair(Account user) {
        String accessToken = generateToken(user);
        String refreshToken = generateRefreshToken(user);

        return TokenPair.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .accessTtl(VALID_DURATION)
                .refreshTtl(REFRESHABLE_DURATION)
                .build();
    }

    // implement 3rd, this function used to verify token whether it invalid or not
    private SignedJWT verifyToken(String token, boolean isRefresh) throws JOSEException, ParseException {
        JWSVerifier verifier = new MACVerifier(SIGNER_KEY.getBytes());
        SignedJWT signedJWT = SignedJWT.parse(token);

        if(invalidatedTokenRepository.existsById(signedJWT.getJWTClaimsSet().getJWTID())) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }
        if (isRefresh) {
            Object tok = signedJWT.getJWTClaimsSet().getClaim("tok");
            if (!"REFRESH".equals(tok)) {
                throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
            }
        }
        // there are 2 kinds of token
        // 1. Token to call API --> isRefresh == null
        // 2. Token to call API to refresh token.
        // Ex: Access token is revoke, but refresh token still exist
        Date expiryTime = (isRefresh)
                ? new Date(signedJWT.getJWTClaimsSet().getIssueTime().toInstant().plus(REFRESHABLE_DURATION, ChronoUnit.SECONDS).toEpochMilli())
                : signedJWT.getJWTClaimsSet().getExpirationTime();

        var verified = signedJWT.verify(verifier);
        if(!(verified && expiryTime.after(new Date()))) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }
        return signedJWT;
    }

    // implement 4th --> func to for user to logout
    // revoke both access and refresh token
    public void logout(String authHeader, String refreshToken)
            throws ParseException, JOSEException {

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);

            SignedJWT sjwt = verifyToken(accessToken, false);
            String jti = sjwt.getJWTClaimsSet().getJWTID();
            Date exp = sjwt.getJWTClaimsSet().getExpirationTime();

            invalidatedTokenRepository.save(
                    InvalidatedToken.builder().id(jti).expiryTime(exp).build()
            );
        }

        if (refreshToken != null && !refreshToken.isBlank()) {
            SignedJWT sjwt = verifyToken(refreshToken, true);
            String jti = sjwt.getJWTClaimsSet().getJWTID();
            Date exp = sjwt.getJWTClaimsSet().getExpirationTime();

            invalidatedTokenRepository.save(
                    InvalidatedToken.builder().id(jti).expiryTime(exp).build()
            );
        }
    }


    // implement 5th --> func to refresh token
    // there are 2 type of token rotation
    // this way is more secure, when user call refresh token api
    // the old token will be invalidated and store to db
    // create a new pair of token
    @Transactional
    public LoginResult refreshFromCookie(String refreshToken) throws ParseException, JOSEException {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.MISSING_REFRESH_TOKEN_COOKIE);
        }

        SignedJWT signed = verifyToken(refreshToken, true);
        String email = signed.getJWTClaimsSet().getSubject();

        Account user = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_EMAIL));

        String oldJti = signed.getJWTClaimsSet().getJWTID();
        Date oldExp   = signed.getJWTClaimsSet().getExpirationTime();

        invalidatedTokenRepository.save(
                InvalidatedToken.builder()
                        .id(oldJti)
                        .expiryTime(oldExp)
                        .build()
        );

        String newAccess  = generateToken(user);
        String newRefresh = generateRefreshToken(user);

        TokenPair pair = TokenPair.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .accessTtl(VALID_DURATION)
                .refreshTtl(REFRESHABLE_DURATION)
                .build();

        String primaryRole = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            primaryRole = user.getRoles().iterator().next().getName();
        }

        return LoginResult.builder()
                .tokenPair(pair)
                .role(primaryRole)
                .build();
    }


    // all of the below func is business, optional implement
    public IntrospectResponse introspect(IntrospectRequest introspectRequest) throws JOSEException, ParseException {
        var token = introspectRequest.getToken();
        boolean valid = true;
        try {
            verifyToken(token, false);
        } catch (AppException e) {
            valid = false;
        }
        return IntrospectResponse.builder().valid(valid).build();
    }

    // in AuthenticationService (injected: LoginMapper loginMapper)
    public LoginResult authenticate(AuthenticationRequest authenticationRequest) {
        Account user = accountRepository.findByEmail(authenticationRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_EMAIL));

        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if (!authenticated) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }

        String access = generateToken(user);
        String refresh = generateRefreshToken(user);

        TokenPair pair = TokenPair.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .accessTtl(VALID_DURATION)
                .refreshTtl(REFRESHABLE_DURATION)
                .build();

        String primaryRole = null;
        if (user.getRoles() != null && !user.getRoles().isEmpty()) {
            primaryRole = user.getRoles().iterator().next().getName();
        }
        return LoginResult.builder()
                .tokenPair(pair)
                .role(primaryRole)
                .build();
    }



    @Transactional
    public RegistrationResult register(AuthenticationRequest request) {
        if(accountRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXIST);
        }

        Role memberRole = roleRepository.findByName("MEMBER");

        Account newUser = new Account();
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setActive(true);
        newUser.setProvider(AuthProvider.LOCAL);
        newUser.setRoles(Set.of(memberRole));

        accountRepository.save(newUser);

        TokenPair tokenPair = generateTokenPair(newUser);

        RegisterResponse registerResponse =
                registerMapper.toRegisterResponse(newUser, tokenPair.getAccessToken());

        return RegistrationResult.builder()
                .registerResponse(registerResponse)
                .tokenPair(tokenPair)
                .build();
    }

}
