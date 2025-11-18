package com.namdang.memos.service.impl;

import com.namdang.memos.config.OryConfig;
import com.namdang.memos.service.OryClientService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class OryClientServiceImpl implements OryClientService {
    private final OryConfig oryConfig;
    private final RestTemplate restTemplate;

    @Override
    public ResponseEntity<String> callWhoAmI(String cookie) {
        String url = oryConfig.getPublicUrl() + "/sessions/whoami";
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.COOKIE, cookie);
        HttpEntity<Void> request = new HttpEntity<>(headers);
        return restTemplate.exchange(url, HttpMethod.GET, request, String.class);
    }
}
