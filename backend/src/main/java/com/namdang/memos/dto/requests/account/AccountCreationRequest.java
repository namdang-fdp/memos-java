package com.namdang.memos.dto.requests.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AccountCreationRequest {
    @NotNull
    private String name;

    @NotNull
    @Email
    private String email;

    @Min(value = 6)
    @NotNull
    private String password;
}
