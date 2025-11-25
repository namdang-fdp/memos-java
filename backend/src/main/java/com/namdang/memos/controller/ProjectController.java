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
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
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
    public ApiResponse<String> createProject(
            @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        projectService.createProject(request, email);
        return ApiResponse.<String>builder()
                .result("Create Project Successfully")
                .build();
    }

    @GetMapping("/project/{id}")
    @PreAuthorize(value = "@projectPermission.canViewAndUpdateProject(#id, authentication)")
    public ApiResponse<CreateProjectResponse> getProject(
            @PathVariable UUID id
    ) {
        CreateProjectResponse response = projectService.getProject(id);
        return ApiResponse.<CreateProjectResponse>builder()
                .result(response)
                .build();
    }

    @GetMapping("/projects")
    public ApiResponse<List<CreateProjectResponse>> getProjects(
            @AuthenticationPrincipal Jwt jwt
    ) {
        String email = jwt.getSubject();
        List<CreateProjectResponse> response = projectService.getProjects(email);
        return ApiResponse.<List<CreateProjectResponse>>builder()
                .result(response)
                .build();
    }

    @PutMapping("/project/{id}")
    @PreAuthorize(value = "@projectPermission.canViewAndUpdateProject(#id, authentication)")
    public ApiResponse<CreateProjectResponse> updateProject(
            @PathVariable UUID id,
            @RequestBody CreateProjectRequest request
    ) {
        CreateProjectResponse response = projectService.updateProject(request, id);
        return ApiResponse.<CreateProjectResponse>builder()
                .result(response)
                .build();
    }

    @DeleteMapping("/project/{id}")
    @PreAuthorize("@projectPermission.canDeleteProject(#id, authentication)")
    public ApiResponse<String> deleteProject(
            @PathVariable UUID id
    ) {
        projectService.deleteProject(id);
        return ApiResponse.<String>builder()
                .result("Delete Project Successfully")
                .build();
    }
}
