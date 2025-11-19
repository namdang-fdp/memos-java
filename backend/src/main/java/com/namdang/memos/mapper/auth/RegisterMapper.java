package com.namdang.memos.mapper.auth;

import com.namdang.memos.dto.responses.auth.RegisterResponse;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.Permission;
import com.namdang.memos.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.stream.Collectors;

// Purpose: Map to RegisterReponse DTO
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface RegisterMapper {
    @Mapping(target = "role", expression = "java(getPrimaryRoleName(account))")
    @Mapping(target = "permissions", expression = "java(getPermissionNames(account))")
    @Mapping(target = "provider", source = "account.provider")
    @Mapping(target = "accessToken", source = "accessToken")
    RegisterResponse toRegisterResponse(Account account, String accessToken);
    default String getPrimaryRoleName(Account account) {
        if (account == null || account.getRoles() == null || account.getRoles().isEmpty()) {
            return null;
        }
        return account.getRoles().stream().findFirst().map(Role::getName).orElse(null);
    }

    default Set<String> getPermissionNames(Account account) {
        if (account == null || account.getRoles() == null || account.getRoles().isEmpty()) {
            return Collections.emptySet();
        }
        return account.getRoles().stream()
                .filter(role -> role.getPermissions() != null)
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .filter(name -> name != null && !name.isBlank())
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

}
