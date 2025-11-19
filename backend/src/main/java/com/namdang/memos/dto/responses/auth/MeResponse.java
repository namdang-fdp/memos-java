package com.namdang.memos.dto.responses.auth;

import com.namdang.memos.enumType.AuthProvider;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.util.Set;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class MeResponse {
    UUID id;
    String email;
    String name;
    AuthProvider provider;
    boolean isActive;
    LocalDate createAt;
    String role;
    Set<String> permissions;
}
