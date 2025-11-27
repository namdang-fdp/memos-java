package com.namdang.memos.mapper.project;

import com.namdang.memos.dto.responses.project.ProjectMemberResponse;
import com.namdang.memos.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMemberMapper {

    @Mapping(source = "account.id", target = "accountId")
    @Mapping(source = "account.name", target = "accountName")
    @Mapping(source = "account.email", target = "accountEmail")
    @Mapping(source = "invitedStatus", target = "inviteStatus")
    ProjectMemberResponse mapToProjectMemberResponse(ProjectMember projectMember);
}
