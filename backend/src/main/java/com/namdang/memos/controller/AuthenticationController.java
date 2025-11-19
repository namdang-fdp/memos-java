package com.namdang.memos.controller;

import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.dto.responses.auth.*;
import com.namdang.memos.service.AuthenticationService;
import com.namdang.memos.service.OryAuthService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationController {
    AuthenticationService authenticationService;
    OryAuthService oryAuthService;

    @NonFinal
    @Value("${ory.session.cookie.name}")
    private String cookieName;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody AuthenticationRequest request) {

        LoginResult result = authenticationService.authenticate(request);

        TokenPair pair = result.getTokenPair();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", pair.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(pair.getRefreshTtl())
                .build();

        AuthenticationResponse body = AuthenticationResponse.builder()
                .authenticated(true)
                .token(pair.getAccessToken())
                .role(result.getRole())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.<AuthenticationResponse>builder().result(body).build());
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegisterResponse>> register(
            @RequestBody AuthenticationRequest request) {

        RegistrationResult result = authenticationService.register(request);

        TokenPair tokenPair = result.getTokenPair();
        RegisterResponse body = result.getRegisterResponse();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokenPair.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(tokenPair.getRefreshTtl())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.<RegisterResponse>builder().result(body).build());
    }


    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MeResponse>> me(
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) throws Exception {

        MeResponse profile = authenticationService.me(authHeader);

        return ResponseEntity.ok(
                ApiResponse.<MeResponse>builder().result(profile).build()
        );
    }


    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(
            @CookieValue(name = "refresh_token", required = false) String refreshToken,
            @RequestHeader(name = HttpHeaders.AUTHORIZATION, required = false) String authHeader
    ) throws Exception {

        authenticationService.logout(authHeader, refreshToken);

        ResponseCookie deleteRefresh = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(0)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, deleteRefresh.toString())
                .body(ApiResponse.<Void>builder().build());
    }


    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refresh(
            @CookieValue(name = "refresh_token", required = false) String refreshToken
    ) throws Exception {
        LoginResult result = authenticationService.refreshFromCookie(refreshToken);
        TokenPair pair = result.getTokenPair();

        ResponseCookie newRefresh = ResponseCookie.from("refresh_token", pair.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(pair.getRefreshTtl())
                .build();

        AuthenticationResponse body = AuthenticationResponse.builder()
                .authenticated(true)
                .token(pair.getAccessToken())
                .role(result.getRole())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefresh.toString())
                .body(ApiResponse.<AuthenticationResponse>builder().result(body).build());
    }

    @PostMapping("/oidc/ory")
    public ResponseEntity<ApiResponse<RegisterResponse>> loginFromOry(
            @CookieValue(name = "${ory.session.cookie.name}", required = false) String oryCookieValue
    ) {
        RegistrationResult result = oryAuthService.loginFromOrySession(cookieName,oryCookieValue);

        TokenPair tokenPair = result.getTokenPair();

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokenPair.getRefreshToken())
                .secure(true)
                .httpOnly(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(tokenPair.getRefreshTtl())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.<RegisterResponse>builder()
                        .result(result.getRegisterResponse())
                        .build());
    }

}
