package com.namdang.memos.dto.requests.account;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Min;
import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AccountUpdateRequest {
    private String name;

    @Min(6)
    private String password;

    private Set<UUID> roles;
}
