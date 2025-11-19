package com.namdang.memos.dto.responses.auth;

import com.namdang.memos.enumType.AuthProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponse {
    private String accessToken;
    private String role;
    private Set<String> permissions;
    private AuthProvider provider;
}
