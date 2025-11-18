package com.namdang.memos.service;

import org.springframework.http.ResponseEntity;

public interface OryClientService {
    ResponseEntity<String> callWhoAmI(String cookie);
}
