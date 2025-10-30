package com.namdang.memos.service;

import com.namdang.memos.dto.requests.account.AccountCreationRequest;
import com.namdang.memos.dto.requests.account.AccountUpdateRequest;
import com.namdang.memos.dto.responses.account.AccountResponse;
import com.namdang.memos.entity.Account;

import java.util.List;
import java.util.UUID;

public interface AccountService {
    UUID createAccount(AccountCreationRequest request);

    List<Account> getAllAccounts();

    AccountResponse getAccountById(UUID id);

    void updateAccount(AccountUpdateRequest request, UUID id);

    void deleteAccount(UUID id);
}
