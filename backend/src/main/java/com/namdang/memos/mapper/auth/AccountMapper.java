package com.namdang.memos.mapper.auth;

import com.namdang.memos.dto.requests.account.AccountCreationRequest;
import com.namdang.memos.dto.requests.account.AccountUpdateRequest;
import com.namdang.memos.dto.responses.account.AccountResponse;
import com.namdang.memos.entity.Account;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public interface AccountMapper {
    Account toAccount(AccountCreationRequest request);
    AccountResponse toAccountResponse(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "password", ignore = true)
    @Mapping(target = "roles", ignore = true)
    void updateFromDto(AccountUpdateRequest request, @MappingTarget Account account);
}

