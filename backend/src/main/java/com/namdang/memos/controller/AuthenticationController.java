package com.namdang.memos.controller;

import com.namdang.memos.dto.requests.LogoutRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.requests.auth.IntrospectRequest;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.dto.responses.auth.*;
import com.namdang.memos.service.AccountService;
import com.namdang.memos.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
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
    AccountService accountService;

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


    @PostMapping("/introspect")
    ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
            throws ParseException, JOSEException {
        return ApiResponse.<IntrospectResponse>builder()
                .result(authenticationService.introspect(request))
                .build();
    }

    @PostMapping("/logout")
    ApiResponse<Void> logout(@RequestBody LogoutRequest request)
            throws ParseException, JOSEException {
        authenticationService.logout(request);
        return ApiResponse.<Void>builder()
                .build();
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

}
