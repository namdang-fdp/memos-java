package com.namdang.memos.controller;

import com.namdang.memos.dto.requests.project.CreateProjectRequest;
import com.namdang.memos.dto.responses.ApiResponse;
import com.namdang.memos.dto.responses.project.CreateProjectResponse;
import com.namdang.memos.service.ProjectService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProjectController {
    ProjectService projectService;

    @PostMapping("/project")
    @PreAuthorize("hasAuthority('PROJECT.CREATE') or hasAuthority('ADMIN.FULL_ACCESS')")
    public ApiResponse<CreateProjectResponse> createProject(
            @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        CreateProjectResponse response = projectService.createProject(request, email);
        return ApiResponse.<CreateProjectResponse>builder()
                .result(response)
                .build();
    }
}
