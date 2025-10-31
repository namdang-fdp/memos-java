package com.namdang.memos.service;

import com.namdang.memos.dto.requests.LogoutRequest;
import com.namdang.memos.dto.requests.RefreshRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.requests.auth.IntrospectRequest;
import com.namdang.memos.dto.responses.auth.AuthenticationResponse;
import com.namdang.memos.dto.responses.auth.IntrospectResponse;
import com.namdang.memos.dto.responses.auth.TokenPair;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.InvalidatedToken;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.InvalidatedTokenRepository;
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
import java.util.Date;
import java.util.StringJoiner;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationService {
    private PasswordEncoder passwordEncoder;
    private AccountRepository accountRepository;
    private InvalidatedTokenRepository invalidatedTokenRepository;

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
    public void logout(LogoutRequest logoutRequest) throws ParseException, JOSEException {
        try {
            var signedToken = verifyToken(logoutRequest.getToken(), true);
            String jit = signedToken.getJWTClaimsSet().getJWTID();
            Date expiryTime = signedToken.getJWTClaimsSet().getExpirationTime();

            InvalidatedToken invalidatedToken = new InvalidatedToken(jit, expiryTime);
            invalidatedTokenRepository.save(invalidatedToken);
        } catch (AppException e) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }
    }

    // implement 5th --> func to refresh token
    // there are 2 type of token rotation
    // this way is more secure, when user call refresh token api
    // the old token will be invalidated and store to db
    // create a new pair of token
    @Transactional
    public TokenPair refreshFromCookie(String refreshToken) throws ParseException, JOSEException {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new AppException(ErrorCode.UNAUTHENTICATED_EXCEPTION);
        }

        // 1) verify refresh token
        SignedJWT signed = verifyToken(refreshToken, true);
        String email = signed.getJWTClaimsSet().getSubject();

        Account user = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_EMAIL));

        // 2) rotate: revoke refresh old token
        String oldJti = signed.getJWTClaimsSet().getJWTID();
        Date oldExp   = signed.getJWTClaimsSet().getExpirationTime();
        invalidatedTokenRepository.save(
                InvalidatedToken.builder().id(oldJti).expiryTime(oldExp).build()
        );

        // 3) new pair token
        String newAccess  = generateToken(user);           // TTL = VALID_DURATION
        String newRefresh = generateRefreshToken(user);    // TTL = REFRESHABLE_DURATION

        return TokenPair.builder()
                .accessToken(newAccess)
                .refreshToken(newRefresh)
                .accessTtl(VALID_DURATION)
                .refreshTtl(REFRESHABLE_DURATION)
                .build();
    }

    // implement 6th --> check whether the token is valid or not
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

    // implement 7th --> authenticate any request with account and password
    public TokenPair authenticate(AuthenticationRequest authenticationRequest) {
        Account user = accountRepository.findByEmail(authenticationRequest.getEmail()).orElseThrow(
                () -> new AppException(ErrorCode.INVALID_EMAIL)
        );
        boolean authenticated = passwordEncoder.matches(authenticationRequest.getPassword(), user.getPassword());
        if(!authenticated) {
            throw new AppException(ErrorCode.INVALID_PASSWORD);
        }
        String access = generateToken(user);
        String refresh = generateRefreshToken(user);

        return TokenPair.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .accessTtl(VALID_DURATION)
                .refreshTtl(REFRESHABLE_DURATION)
                .build();
    }

}
