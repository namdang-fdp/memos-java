package com.namdang.memos.service;

import com.namdang.memos.dto.requests.invite.ProjectInviteRequest;
import com.namdang.memos.dto.responses.invite.InviteInfoResponse;
import com.namdang.memos.dto.responses.project.ProjectMemberResponse;

import java.util.UUID;

public interface InviteService {
    // return project member response so FE can directly update user to list of project's members
    ProjectMemberResponse createInvite(ProjectInviteRequest projectInviteRequest, String email, UUID projectId);

    InviteInfoResponse getInviteInfo(String token);

    ProjectMemberResponse acceptInvite(String token, String currentUserEmail);

    void declineInvite(String token, String currentUserEmail);
}
