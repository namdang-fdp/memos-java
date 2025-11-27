package com.namdang.memos.controller;

import com.namdang.memos.dto.requests.invite.ProjectInviteRequest;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.dto.responses.invite.InviteInfoResponse;
import com.namdang.memos.dto.responses.project.ProjectMemberResponse;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.service.InviteService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InviteController {

    InviteService inviteService;
    AccountRepository accountRepository;

    @PostMapping("/invite/project/{id}")
    @PreAuthorize("@projectPermission.canInviteToProject(#id, authentication)")
    public ApiResponse<ProjectMemberResponse> createInvite(
            @PathVariable UUID id,
            @RequestBody ProjectInviteRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        ProjectMemberResponse response = inviteService.createInvite(request, email, id);

        return ApiResponse.<ProjectMemberResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/invite/info/{id}")
    public ApiResponse<InviteInfoResponse> getInviteInfo(
            @PathVariable String id
    ) {
        InviteInfoResponse response = inviteService.getInviteInfo(id);
        return ApiResponse.<InviteInfoResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/invite/{id}/accept")
    @PreAuthorize("hasRole('MEMBER') or hasAuthority('ADMIN.FULL_ACCESS')")
    public ApiResponse<ProjectMemberResponse> acceptInvite(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        ProjectMemberResponse response = inviteService.acceptInvite(id, email);
        return ApiResponse.<ProjectMemberResponse>builder()
                .result(response)
                .build();
    }

    @PostMapping("/invite/{id}/decline")
    @PreAuthorize("hasRole('MEMBER') or hasAuthority('ADMIN.FULL_ACCESS')")
    public ApiResponse<String> declineInvite(
            @PathVariable String id,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        inviteService.declineInvite(id, email);
        return ApiResponse.<String>builder()
                .result("Decline invite successfully")
                .build();
    }
}
