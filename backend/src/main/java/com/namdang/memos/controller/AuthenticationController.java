package com.namdang.memos.controller;

import com.namdang.memos.dto.requests.LogoutRequest;
import com.namdang.memos.dto.requests.RefreshRequest;
import com.namdang.memos.dto.requests.account.AccountCreationRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.requests.auth.IntrospectRequest;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.dto.responses.auth.AuthenticationResponse;
import com.namdang.memos.dto.responses.auth.IntrospectResponse;
import com.namdang.memos.service.AccountService;
import com.namdang.memos.service.AuthenticationService;
import com.nimbusds.jose.JOSEException;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody AuthenticationRequest request) {
        var result = authenticationService.authenticate(request);
        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();
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
    ApiResponse<AuthenticationResponse> authenticate(@RequestBody RefreshRequest request)
            throws ParseException, JOSEException {
        var result = authenticationService.refreshToken(request);

        return ApiResponse.<AuthenticationResponse>builder()
                .result(result)
                .build();

    }

    @PostMapping("/register")
    public ApiResponse<UUID> register(@Valid @RequestBody AccountCreationRequest request) {
        var result = accountService.createAccount(request);
        return ApiResponse.<UUID>builder().result(result).build();
    }
}
