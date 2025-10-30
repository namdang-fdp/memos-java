package com.namdang.memos.controller;

import com.namdang.memos.dto.requests.account.AccountUpdateRequest;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.dto.responses.account.AccountResponse;
import com.namdang.memos.entity.Account;
import com.namdang.memos.service.AccountService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AccountController {
    AccountService accountService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<List<Account>> getAccounts() {
        return ApiResponse.<List<Account>>builder().result(accountService.getAllAccounts()).build();
    }


    @GetMapping("/{id}")
    public ApiResponse<AccountResponse> getAccountById(@PathVariable UUID id) {
        return ApiResponse.<AccountResponse>builder().result(accountService.getAccountById(id)).build();
    }

    @PatchMapping("/{id}")
    public ApiResponse<String> updateAccount(@PathVariable UUID id, @RequestBody AccountUpdateRequest request) {
        accountService.updateAccount(request, id);
        return ApiResponse.<String>builder().result("Updated account successfully").build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('STAFF')")
    public ApiResponse<String> deleteAccount(@PathVariable UUID id) {
        accountService.deleteAccount(id);
        return ApiResponse.<String>builder().result("Delete account successfully").build();
    }

}
