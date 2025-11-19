package com.namdang.memos.service;

import com.namdang.memos.dto.responses.auth.TokenPair;
import com.namdang.memos.dto.responses.ory.OryResponse;
import org.springframework.http.ResponseEntity;

public interface OryAuthService {
    ResponseEntity<OryResponse> callWhoAmI(String cookie);

    TokenPair loginFromOrySession(String cookie);
}
