package com.namdang.memos.dto.responses.auth;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RegistrationResult {
    private RegisterResponse registerResponse;
    private TokenPair tokenPair;
}