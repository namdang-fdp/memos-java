package com.namdang.memos.service.impl;

import com.namdang.memos.dto.requests.project.CreateProjectRequest;
import com.namdang.memos.dto.responses.project.CreateProjectResponse;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.Project;
import com.namdang.memos.entity.ProjectMember;
import com.namdang.memos.enumType.InviteStatus;
import com.namdang.memos.enumType.ProjectRole;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.mapper.project.ProjectMapper;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.ProjectMemberRepository;
import com.namdang.memos.repository.ProjectRepository;
import com.namdang.memos.service.ProjectService;
import com.namdang.memos.validator.ProjectValidator;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMemberRepository projectMemberRepository;
    AccountRepository accountRepository;
    ProjectMapper projectMapper;
    ProjectValidator projectValidator;

    // Rule:
    // If name has one word --> get 2 chars + (countProject + 1)
    // Ex: Test --> TE-1
    // If name has more than one word --> get 1st chars of words (max 3) + (countProject + 1)
    // Ex: Test Project --> TP-1
    // Ex: Test Project Memos Final --> TPM-1
    private String generateProjectKey(String projectName) {
        if(projectName == null || projectName.trim().isEmpty()) {
            projectName = "PROJECT";
        }

        String upper = projectName.trim().toUpperCase();
        String[] words = upper.split("\\s+");
        String prefix;

        if(words.length == 1) {
            String word = words[0].replaceAll("[^A-Z0-9]", "");
            if(word.length() >= 2) {
                prefix = word.substring(0, 2);
            } else {
                prefix = word;
            }
        } else {
            StringBuilder builder = new StringBuilder();
            for(int i = 0; i < words.length && builder.length() < 3; i++) {
                String cleaned = words[i].replaceAll("[^A-Z0-9]", "");
                if(!cleaned.isEmpty()) {
                    builder.append(cleaned.charAt(0));
                }
            }
            prefix = builder.toString();
        }

        if(prefix.isEmpty()) {
            prefix = "PR";
        }
        long count = projectRepository.count();
        long nextNumber = count + 1;

        return prefix + "-" + nextNumber;
    }


    @Override
    @Transactional
    public CreateProjectResponse createProject(CreateProjectRequest request, String creatorEmail) {
        projectValidator.validatorProjectName(request.getName());
        Account projectCreator = accountRepository.findByEmail(creatorEmail)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_EMAIL));

        Project project = projectMapper.mapToProject(request);
        String projectKey = generateProjectKey(request.getName());
        project.setProjectKey(projectKey);
        project.setCreatedBy(projectCreator);
        project = projectRepository.save(project);

        ProjectMember owner = new ProjectMember();
        owner.setProject(project);
        owner.setAccount(projectCreator);
        owner.setJoinedAt(LocalDateTime.now());
        owner.setRole(ProjectRole.OWNER);
        owner.setInvitedEmail(projectCreator.getEmail());
        owner.setInvitedStatus(InviteStatus.ACCEPTED);

        projectMemberRepository.save(owner);

        return projectMapper.mapToCreateProjectResponse(project);
    }
}
