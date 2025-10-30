package com.namdang.memos.service.impl;

import com.namdang.memos.dto.requests.account.AccountCreationRequest;
import com.namdang.memos.dto.requests.account.AccountUpdateRequest;
import com.namdang.memos.dto.responses.account.AccountResponse;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.Role;
import com.namdang.memos.enumType.PreDefinedRole;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.mapper.AccountMapper;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.RoleRepository;
import com.namdang.memos.service.AccountService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.*;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@Transactional
public class AccountServiceImpl implements AccountService {
    AccountRepository accountRepository;
    AccountMapper accountMapper;
    PasswordEncoder passwordEncoder;
    RoleRepository roleRepository;

    @Override
    public UUID createAccount(AccountCreationRequest request) {
        Optional<Account> existingAccount = accountRepository.findByEmail(request.getEmail());
        if(existingAccount.isPresent()) {
            throw new AppException(ErrorCode.DUPLICATE_EMAIL);
        }
        Account account = accountMapper.toAccount(request);
        Role role = roleRepository.findByName(PreDefinedRole.MEMBER.name());
        account.getRoles().add(role);
        account.setPassword(passwordEncoder.encode(account.getPassword()));
        Account saveAccount = accountRepository.save(account);
        return saveAccount.getId();
    }

    @Override
    @Transactional(readOnly = true)
    @PreAuthorize("hasRole('ADMIN')")
    public List<Account> getAllAccounts() {
        return accountRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    @PostAuthorize("returnObject.email == authentication.name")
    public AccountResponse getAccountById(UUID id) {
        Account account = accountRepository.findById(id).orElseThrow(() ->
                new AppException(ErrorCode.INVALID_ACCOUNT));
        return accountMapper.toAccountResponse(account);
    }

    @Override
    public void updateAccount(AccountUpdateRequest request, UUID id) {

        Account existingAccount = accountRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_ACCOUNT));
        accountMapper.updateFromDto(request, existingAccount);
        if (!Objects.isNull(request.getPassword())) {
            existingAccount.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (!CollectionUtils.isEmpty(request.getRoles())) {
            var roles = roleRepository.findAllById(request.getRoles());
            existingAccount.setRoles(new HashSet<>(roles));
        }
        accountRepository.save(existingAccount);
    }

    @Override
    public void deleteAccount(UUID id) {
        accountRepository.deleteById(id);
    }
}
