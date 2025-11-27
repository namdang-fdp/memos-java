package com.namdang.memos.service.impl;

import com.namdang.memos.dto.requests.invite.ProjectInviteRequest;
import com.namdang.memos.dto.responses.invite.InviteInfoResponse;
import com.namdang.memos.dto.responses.project.ProjectMemberResponse;
import com.namdang.memos.entity.Account;
import com.namdang.memos.entity.Project;
import com.namdang.memos.entity.ProjectMember;
import com.namdang.memos.enumType.InviteStatus;
import com.namdang.memos.enumType.ProjectRole;
import com.namdang.memos.exception.AppException;
import com.namdang.memos.exception.ErrorCode;
import com.namdang.memos.mapper.project.InviteMapper;
import com.namdang.memos.mapper.project.ProjectMemberMapper;
import com.namdang.memos.repository.AccountRepository;
import com.namdang.memos.repository.ProjectMemberRepository;
import com.namdang.memos.repository.ProjectRepository;
import com.namdang.memos.service.InviteService;
import com.namdang.memos.service.MailService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class InviteServiceImpl implements InviteService {
    ProjectRepository projectRepository;
    AccountRepository accountRepository;
    ProjectMemberRepository projectMemberRepository;
    ProjectMemberMapper projectMemberMapper;
    InviteMapper inviteMapper;
    MailService mailService;

    static int INVITE_EXPIRE_DAYS = 7;

    @Override
    @Transactional
    public ProjectMemberResponse createInvite(ProjectInviteRequest request, String email, UUID projectId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new AppException(ErrorCode.PROJECT_NOT_FOUND));

        Account projectOwner = accountRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.INVALID_EMAIL));

        // check if user is already have account or not
        Account existingAccount = accountRepository.findByEmail(request.getTargetUserEmail())
                .orElse(null);

        if(existingAccount != null && projectMemberRepository.existsByProject_IdAndAccount_Id(projectId, existingAccount.getId())) {
            throw new AppException(ErrorCode.MEMBER_ALREADY_EXISTED);
        }

        if(projectMemberRepository.existsByProject_IdAndInvitedEmailAndInvitedStatus(projectId, request.getTargetUserEmail(), InviteStatus.PENDING)) {
            throw new AppException(ErrorCode.INVITE_ALREADY_SENT);
        }

        ProjectMember member = new ProjectMember();
        member.setProject(project);
        member.setRole(ProjectRole.MEMBER);
        member.setInvitedEmail(request.getTargetUserEmail());
        member.setInvitedStatus(InviteStatus.PENDING);
        member.setInviteToken(UUID.randomUUID().toString());
        member.setInviteExpiredAt(LocalDateTime.now().plusDays(INVITE_EXPIRE_DAYS));

        if(existingAccount != null) {
            member.setAccount(existingAccount);
        }
        projectMemberRepository.save(member);

        String inviterName = projectOwner.getEmail();
        mailService.sendProjectInviteMail(
                request.getTargetUserEmail(),
                project.getName(),
                inviterName,
                member.getInviteToken()
        );
        return projectMemberMapper.mapToProjectMemberResponse(member);
    }

    @Override
    @Transactional
    public InviteInfoResponse getInviteInfo(String token) {
        ProjectMember projectMember = projectMemberRepository.findByInviteToken(token)
                .orElseThrow(() -> new AppException(ErrorCode.INVITE_NOT_FOUND));

        if(projectMember.getInviteExpiredAt().isBefore(LocalDateTime.now())) {
            throw new AppException(ErrorCode.INVITATION_IS_EXPIRED);
        }

        return inviteMapper.mapToInviteInfoResponse(projectMember);
    }
}
