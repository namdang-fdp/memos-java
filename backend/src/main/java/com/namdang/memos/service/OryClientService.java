package com.namdang.memos.service;

import com.namdang.memos.dto.responses.ory.OryResponse;
import org.springframework.http.ResponseEntity;

public interface OryClientService {
    ResponseEntity<OryResponse> callWhoAmI(String cookie);
}
