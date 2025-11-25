package com.namdang.memos.security;

import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.ProjectMember;
import com.namdang.memos.enumType.ProjectRole;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.ProjectMemberRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.UUID;


@Component("projectPermission")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectPermission {
    AccountRepository accountRepository;
    ProjectMemberRepository projectMemberRepository;

    // admin can do everything
    private boolean hasAdminFullAccess(Authentication authentication) {
        return authentication.getAuthorities().stream()
                .anyMatch(a -> "ADMIN.FULL_ACCESS".equals(a.getAuthority()));
    }

    private Account getCurrentAccount(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        String email = jwt.getSubject();
        return accountRepository.findByEmail(email).orElse(null);
    }

    // just owner or admin can delete project
    public boolean canDeleteProject(UUID projectId, Authentication authentication) {
        if(authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        if(hasAdminFullAccess(authentication)) {
            return true;
        }
        Account account = getCurrentAccount(authentication);
        if(account == null) {
            return false;
        }
        return projectMemberRepository
                .findByProjectIdAndAccountId(projectId,account.getId())
                .map(ProjectMember::getRole)
                .map(role -> role == ProjectRole.OWNER)
                .orElse(false);
    }
    // admin + owner + member (of project) can update project and view project
    public boolean canViewAndUpdateProject(UUID projectId, Authentication authentication) {

        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        if (hasAdminFullAccess(authentication)) {
            return true;
        }

        Account account = getCurrentAccount(authentication);
        if (account == null) {
            return false;
        }

        return projectMemberRepository
                .findByProjectIdAndAccountId(projectId, account.getId())
                .isPresent();
    }

}
