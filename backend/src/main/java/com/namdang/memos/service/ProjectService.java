package com.namdang.memos.service;

import com.namdang.memos.dto.requests.project.CreateProjectRequest;
import com.namdang.memos.dto.responses.project.CreateProjectResponse;
import java.util.List;
import java.util.UUID;

public interface ProjectService {
    CreateProjectResponse createProject(CreateProjectRequest request, String creatorEmail);

    void deleteProject(UUID projectId);

    CreateProjectResponse updateProject(CreateProjectRequest request, UUID projectId);

    CreateProjectResponse getProject(UUID projectId);

    List<CreateProjectResponse> getProjects(String email);
}
