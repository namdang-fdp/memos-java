package com.namdang.memos.mapper.project;

import com.namdang.memos.dto.responses.invite.InviteInfoResponse;
import com.namdang.memos.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface InviteMapper {
    @Mapping(source = "project.name", target = "projectName")
    @Mapping(source = "project.id", target = "projectId")
    @Mapping(source = "project.createdBy.name", target = "inviterName")
    @Mapping(source = "invitedEmail", target = "invitedEmail")
    @Mapping(source = "invitedStatus", target = "status")
    @Mapping(source = "inviteExpiredAt", target = "inviteExpiredAt")
    InviteInfoResponse mapToInviteInfoResponse(ProjectMember member);
}
