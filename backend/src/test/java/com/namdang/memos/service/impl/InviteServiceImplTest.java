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
import com.namdang.memos.service.MailService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InviteServiceImplTest {

    @Mock ProjectRepository projectRepository;
    @Mock AccountRepository accountRepository;
    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock ProjectMemberMapper projectMemberMapper;
    @Mock InviteMapper inviteMapper;
    @Mock MailService mailService;

    @InjectMocks InviteServiceImpl inviteService;

    private static ProjectInviteRequest req(String targetEmail) {
        ProjectInviteRequest r = new ProjectInviteRequest();
        r.setTargetUserEmail(targetEmail);
        return r;
    }

    @Test
    void createInvite_projectNotFound_throwsProjectNotFound() {
        UUID projectId = UUID.randomUUID();
        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteService.createInvite(req("t@test.com"), "owner@test.com", projectId))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);

        verifyNoInteractions(mailService, projectMemberRepository, accountRepository);
    }

    @Test
    void createInvite_ownerInvalidEmail_throwsInvalidEmail() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setName("P");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteService.createInvite(req("t@test.com"), "owner@test.com", projectId))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);

        verifyNoInteractions(mailService);
        verify(projectMemberRepository, never()).save(any());
    }

    @Test
    void createInvite_targetAlreadyMember_throwsMemberAlreadyExisted() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setName("P");

        Account owner = new Account();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@test.com");

        Account existing = new Account();
        UUID existingId = UUID.randomUUID();
        existing.setId(existingId);
        existing.setEmail("t@test.com");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(accountRepository.findByEmail("t@test.com")).thenReturn(Optional.of(existing));
        when(projectMemberRepository.existsByProject_IdAndAccount_Id(projectId, existingId)).thenReturn(true);

        assertThatThrownBy(() -> inviteService.createInvite(req("t@test.com"), "owner@test.com", projectId))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.MEMBER_ALREADY_EXISTED);

        verify(projectMemberRepository, never()).save(any());
        verifyNoInteractions(mailService);
    }

    @Test
    void createInvite_pendingInviteAlreadySent_throwsInviteAlreadySent() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setName("P");

        Account owner = new Account();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@test.com");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(accountRepository.findByEmail("t@test.com")).thenReturn(Optional.empty());

        when(projectMemberRepository.existsByProject_IdAndInvitedEmailAndInvitedStatus(
                projectId, "t@test.com", InviteStatus.PENDING
        )).thenReturn(true);

        assertThatThrownBy(() -> inviteService.createInvite(req("t@test.com"), "owner@test.com", projectId))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVITE_ALREADY_SENT);

        verify(projectMemberRepository, never()).save(any());
        verifyNoInteractions(mailService);
    }

    @Test
    void createInvite_success_existingAccount_setsAccount_savesMember_sendsMail_andReturnsMapped() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setName("My Project");

        Account owner = new Account();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@test.com");

        Account existing = new Account();
        existing.setId(UUID.randomUUID());
        existing.setEmail("t@test.com");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(accountRepository.findByEmail("t@test.com")).thenReturn(Optional.of(existing));

        when(projectMemberRepository.existsByProject_IdAndAccount_Id(projectId, existing.getId())).thenReturn(false);
        when(projectMemberRepository.existsByProject_IdAndInvitedEmailAndInvitedStatus(
                projectId, "t@test.com", InviteStatus.PENDING
        )).thenReturn(false);

        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(inv -> inv.getArgument(0));

        ProjectMemberResponse mapped = mock(ProjectMemberResponse.class);
        when(projectMemberMapper.mapToProjectMemberResponse(any(ProjectMember.class))).thenReturn(mapped);

        ProjectInviteRequest request = req("t@test.com");

        ProjectMemberResponse res = inviteService.createInvite(request, "owner@test.com", projectId);

        assertThat(res).isSameAs(mapped);

        ArgumentCaptor<ProjectMember> captor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(captor.capture());

        ProjectMember saved = captor.getValue();
        assertThat(saved.getProject()).isSameAs(project);
        assertThat(saved.getRole()).isEqualTo(ProjectRole.MEMBER);
        assertThat(saved.getInvitedEmail()).isEqualTo("t@test.com");
        assertThat(saved.getInvitedStatus()).isEqualTo(InviteStatus.PENDING);
        assertThat(saved.getInviteToken()).isNotBlank();
        assertThat(saved.getInviteExpiredAt()).isAfter(LocalDateTime.now().plusDays(6));
        assertThat(saved.getAccount()).isSameAs(existing);

        verify(mailService).sendProjectInviteMail(
                eq("t@test.com"),
                eq("My Project"),
                eq("owner@test.com"),
                eq(saved.getInviteToken())
        );
    }

    @Test
    void createInvite_success_noExistingAccount_savesMember_withoutAccount_andSendsMail() {
        UUID projectId = UUID.randomUUID();
        Project project = new Project();
        project.setId(projectId);
        project.setName("My Project");

        Account owner = new Account();
        owner.setId(UUID.randomUUID());
        owner.setEmail("owner@test.com");

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(project));
        when(accountRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(owner));
        when(accountRepository.findByEmail("t@test.com")).thenReturn(Optional.empty());

        when(projectMemberRepository.existsByProject_IdAndInvitedEmailAndInvitedStatus(
                projectId, "t@test.com", InviteStatus.PENDING
        )).thenReturn(false);

        when(projectMemberRepository.save(any(ProjectMember.class))).thenAnswer(inv -> inv.getArgument(0));
        when(projectMemberMapper.mapToProjectMemberResponse(any())).thenReturn(mock(ProjectMemberResponse.class));

        inviteService.createInvite(req("t@test.com"), "owner@test.com", projectId);

        ArgumentCaptor<ProjectMember> captor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(captor.capture());

        ProjectMember saved = captor.getValue();
        assertThat(saved.getAccount()).isNull();

        verify(mailService).sendProjectInviteMail(
                eq("t@test.com"),
                eq("My Project"),
                eq("owner@test.com"),
                eq(saved.getInviteToken())
        );
    }

    @Test
    void getInviteInfo_notFound_throwsInviteNotFound() {
        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteService.getInviteInfo("t"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVITE_NOT_FOUND);
    }

    @Test
    void getInviteInfo_expired_throwsInvitationIsExpired() {
        ProjectMember pm = new ProjectMember();
        pm.setInviteToken("t");
        pm.setInviteExpiredAt(LocalDateTime.now().minusMinutes(1));

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));

        assertThatThrownBy(() -> inviteService.getInviteInfo("t"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVITATION_IS_EXPIRED);

        verify(inviteMapper, never()).mapToInviteInfoResponse(any());
    }

    @Test
    void getInviteInfo_success_mapsResponse() {
        ProjectMember pm = new ProjectMember();
        pm.setInviteToken("t");
        pm.setInviteExpiredAt(LocalDateTime.now().plusDays(1));

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));

        InviteInfoResponse mapped = mock(InviteInfoResponse.class);
        when(inviteMapper.mapToInviteInfoResponse(pm)).thenReturn(mapped);

        InviteInfoResponse res = inviteService.getInviteInfo("t");

        assertThat(res).isSameAs(mapped);
    }

    @Test
    void acceptInvite_notFound_throwsInviteNotFound() {
        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteService.acceptInvite("t", "u@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVITE_NOT_FOUND);
    }

    @Test
    void acceptInvite_invalidUserEmail_throwsInvalidEmail() {
        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("u@test.com");
        pm.setInvitedStatus(InviteStatus.PENDING);
        pm.setInviteExpiredAt(LocalDateTime.now().plusDays(1));

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));
        when(accountRepository.findByEmail("u@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteService.acceptInvite("t", "u@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);
    }

    @Test
    void acceptInvite_invitedEmailMismatch_throwsInvalidInviteEmail() {
        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("target@test.com");
        pm.setInvitedStatus(InviteStatus.PENDING);
        pm.setInviteExpiredAt(LocalDateTime.now().plusDays(1));

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));
        when(accountRepository.findByEmail("other@test.com")).thenReturn(Optional.of(new Account()));

        assertThatThrownBy(() -> inviteService.acceptInvite("t", "other@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INVITE_EMAIL);
    }

    @Test
    void acceptInvite_notPending_throwsInvalidInvitation() {
        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("u@test.com");
        pm.setInvitedStatus(InviteStatus.ACCEPTED);
        pm.setInviteExpiredAt(LocalDateTime.now().plusDays(1));

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));
        when(accountRepository.findByEmail("u@test.com")).thenReturn(Optional.of(new Account()));

        assertThatThrownBy(() -> inviteService.acceptInvite("t", "u@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INVITATION);
    }

    @Test
    void acceptInvite_expired_throwsInvitationIsExpired() {
        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("u@test.com");
        pm.setInvitedStatus(InviteStatus.PENDING);
        pm.setInviteExpiredAt(LocalDateTime.now().minusSeconds(1));

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));
        when(accountRepository.findByEmail("u@test.com")).thenReturn(Optional.of(new Account()));

        assertThatThrownBy(() -> inviteService.acceptInvite("t", "u@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVITATION_IS_EXPIRED);
    }

    @Test
    void acceptInvite_success_updatesMemberFields_andReturnsMapped() {
        Account acc = new Account();
        acc.setId(UUID.randomUUID());
        acc.setEmail("u@test.com");

        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("u@test.com");
        pm.setInvitedStatus(InviteStatus.PENDING);
        pm.setInviteToken("token");
        pm.setInviteExpiredAt(LocalDateTime.now().plusDays(1));

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));
        when(accountRepository.findByEmail("u@test.com")).thenReturn(Optional.of(acc));

        ProjectMemberResponse mapped = mock(ProjectMemberResponse.class);
        when(projectMemberMapper.mapToProjectMemberResponse(pm)).thenReturn(mapped);

        ProjectMemberResponse res = inviteService.acceptInvite("t", "u@test.com");

        assertThat(res).isSameAs(mapped);
        assertThat(pm.getAccount()).isSameAs(acc);
        assertThat(pm.getJoinedAt()).isNotNull();
        assertThat(pm.getInvitedStatus()).isEqualTo(InviteStatus.ACCEPTED);
        assertThat(pm.getInviteToken()).isNull();
    }

    @Test
    void declineInvite_notFound_throwsInvalidInvitation() {
        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> inviteService.declineInvite("t", "u@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INVITATION);
    }

    @Test
    void declineInvite_notPending_returnsWithoutChanges() {
        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("u@test.com");
        pm.setInvitedStatus(InviteStatus.ACCEPTED);
        pm.setInviteToken("token");

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));

        inviteService.declineInvite("t", "u@test.com");

        assertThat(pm.getInvitedStatus()).isEqualTo(InviteStatus.ACCEPTED);
        assertThat(pm.getInviteToken()).isEqualTo("token");
    }

    @Test
    void declineInvite_emailMismatch_throwsInvalidInviteEmail() {
        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("u@test.com");
        pm.setInvitedStatus(InviteStatus.PENDING);

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));

        assertThatThrownBy(() -> inviteService.declineInvite("t", "other@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_INVITE_EMAIL);
    }

    @Test
    void declineInvite_success_setsDeclined_andClearsToken() {
        ProjectMember pm = new ProjectMember();
        pm.setInvitedEmail("u@test.com");
        pm.setInvitedStatus(InviteStatus.PENDING);
        pm.setInviteToken("token");

        when(projectMemberRepository.findByInviteToken("t")).thenReturn(Optional.of(pm));

        inviteService.declineInvite("t", "u@test.com");

        assertThat(pm.getInvitedStatus()).isEqualTo(InviteStatus.DECLINED);
        assertThat(pm.getInviteToken()).isNull();
    }
}
