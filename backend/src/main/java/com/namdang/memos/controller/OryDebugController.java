package com.namdang.memos.controller;

import com.namdang.memos.dto.responses.ory.OryResponse;
import com.namdang.memos.service.OryClientService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/ory")
@RequiredArgsConstructor
@Slf4j
public class OryDebugController {

    private final OryClientService oryClientService;

    @GetMapping("/whoami-debug")
    public ResponseEntity<OryResponse> debugWhoAmI(HttpServletRequest request) {

        String cookie = request.getHeader("Cookie");
        if (cookie == null || cookie.isBlank()) {
            log.warn("No Cookie header found in request");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        ResponseEntity<OryResponse> response = oryClientService.callWhoAmI(cookie);
        OryResponse body = response.getBody();

        log.info("Ory whoami response: {}", body);

        // Trả nguyên response cho FE/postman xem luôn
        return ResponseEntity
                .status(response.getStatusCode())
                .body(body);
    }
}
