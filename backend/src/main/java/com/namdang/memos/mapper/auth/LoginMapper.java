package com.namdang.memos.mapper.auth;

import com.namdang.memos.dto.responses.auth.AuthenticationResponse;
import com.namdang.memos.entity.Account;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LoginMapper {

    @Mapping(target = "role", expression = "java(getPrimaryRoleName(account))")
    @Mapping(target = "token", source = "accessToken")
    @Mapping(target = "authenticated", constant = "true")
    AuthenticationResponse toLoginResponse(Account account, String accessToken);

    default String getPrimaryRoleName(Account account) {
        if (account == null || account.getRoles() == null || account.getRoles().isEmpty()) return null;
        return account.getRoles().iterator().next().getName();
    }
}


