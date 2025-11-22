package com.namdang.memos.mapper.project;

import com.namdang.memos.dto.requests.project.CreateProjectRequest;
import com.namdang.memos.dto.responses.project.CreateProjectResponse;
import com.namdang.memos.entity.Project;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProjectMapper {
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "archived", ignore = true)
    @Mapping(target = "projectKey", ignore = true)
    Project mapToProject(CreateProjectRequest createProjectRequest);

    @Mapping(source = "createdBy.id", target = "createdById")
    CreateProjectResponse mapToCreateProjectResponse(Project project);
}
