package com.namdang.memos.service;

import com.namdang.memos.dto.responses.auth.RegistrationResult;
import com.namdang.memos.dto.responses.ory.OryResponse;
import org.springframework.http.ResponseEntity;

public interface OryAuthService {
    ResponseEntity<OryResponse> callWhoAmI(String cookieName, String cookieValue);

    RegistrationResult loginFromOrySession(String cookieName, String cookieValue);
}
