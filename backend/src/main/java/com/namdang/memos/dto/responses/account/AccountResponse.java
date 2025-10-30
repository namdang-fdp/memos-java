package com.namdang.memos.dto.responses.account;

import com.namdang.memos.entity.Role;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Setter
@Getter
@Builder
public class AccountResponse {
    private String email;
    private String name;
    private boolean isActive = true;
    private Set<Role> roles = new HashSet<>();
}
