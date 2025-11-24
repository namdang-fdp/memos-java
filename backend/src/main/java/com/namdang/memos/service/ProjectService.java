package com.namdang.memos.service;

import com.namdang.memos.dto.requests.project.CreateProjectRequest;
import com.namdang.memos.dto.responses.project.CreateProjectResponse;

public interface ProjectService {
    CreateProjectResponse createProject(CreateProjectRequest request, String creatorEmail);
}
