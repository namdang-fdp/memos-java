package com.namdang.memos.service;

import com.namdang.memos.dto.requests.invite.ProjectInviteRequest;
import com.namdang.memos.dto.responses.project.ProjectMemberResponse;

import java.util.UUID;

public interface InviteService {
    // return project member response so FE can directly updated user to list of project's members
    ProjectMemberResponse createInvite(ProjectInviteRequest projectInviteRequest, String email, UUID projectId);
}
