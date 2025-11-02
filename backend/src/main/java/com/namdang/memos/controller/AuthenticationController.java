package com.namdang.memos.controller;

import com.namdang.memos.dto.requests.LogoutRequest;
import com.namdang.memos.dto.requests.RefreshRequest;
import com.namdang.memos.dto.requests.account.AccountCreationRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.requests.auth.IntrospectRequest;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.dto.responses.auth.AuthenticationResponse;
import com.namdang.memos.dto.responses.auth.IntrospectResponse;
import com.namdang.memos.dto.responses.auth.TokenPair;
import com.namdang.memos.service.AccountService;
import com.namdang.memos.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.UUID;

@RestController
@RequestMapping("/auth")
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@RequiredArgsConstructor
public class AuthenticationController {
    AuthenticationService authenticationService;
    AccountService accountService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(@RequestBody AuthenticationRequest request) {
        // After login, received the access token (short ttl) and refresh token (long ttl)
        // access token response to FE to attach to header "Bearer" when call api
        // refresh token put in the cookies
        TokenPair pair = authenticationService.authenticate(request);

        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", pair.getRefreshToken())
                .httpOnly(true).secure(true)
                .sameSite("None")
                .path("/auth")
                .maxAge(pair.getRefreshTtl())
                .build();

        AuthenticationResponse body = AuthenticationResponse.builder()
                .authenticated(true)
                .token(pair.getAccessToken())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body(ApiResponse.<AuthenticationResponse>builder().result(body).build());
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
            @CookieValue(name = "refresh_token", required = false) String refreshToken) throws Exception {

        TokenPair pair = authenticationService.refreshFromCookie(refreshToken);

        ResponseCookie newRefresh = ResponseCookie.from("refresh_token", pair.getRefreshToken())
                .httpOnly(true).secure(true).sameSite("None")
                .path("/auth").maxAge(pair.getRefreshTtl()).build();

        var body = AuthenticationResponse.builder()
                .authenticated(true)
                .token(pair.getAccessToken())
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, newRefresh.toString())
                .body(ApiResponse.<AuthenticationResponse>builder().result(body).build());
    }
}
