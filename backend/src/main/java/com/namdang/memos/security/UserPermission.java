package com.namdang.memos.security;

import com.namdang.memos.repository.AccountRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class UserPermission {

    AccountRepository accountRepository;

    public boolean isAdminByEmail(String email) {
        if (email == null) return false;

        return accountRepository.findByEmail(email)
                .map(account -> account.getRoles().stream()
                        .flatMap(r -> r.getPermissions().stream())
                        .anyMatch(p -> "ADMIN.FULL_ACCESS".equals(p.getName()))
                )
                .orElse(false);
    }
}
