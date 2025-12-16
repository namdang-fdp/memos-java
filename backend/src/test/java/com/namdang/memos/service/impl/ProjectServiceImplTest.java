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
import com.namdang.memos.security.UserPermission;
import com.namdang.memos.validator.ProjectValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectServiceImplTest {

    @Mock ProjectRepository projectRepository;
    @Mock ProjectMemberRepository projectMemberRepository;
    @Mock AccountRepository accountRepository;
    @Mock ProjectMapper projectMapper;
    @Mock ProjectValidator projectValidator;
    @Mock UserPermission userPermission;

    @InjectMocks ProjectServiceImpl projectService;

    private static CreateProjectRequest req(String name) {
        CreateProjectRequest r = new CreateProjectRequest();
        r.setName(name);
        r.setImageUrl("img");
        r.setDescription("desc");
        return r;
    }

    @Test
    void createProject_invalidName_callsValidator_andThrowsIfValidatorThrows() {
        CreateProjectRequest request = req("  ");

        doThrow(new AppException(ErrorCode.INVALID_PROJECT_NAME))
                .when(projectValidator).validatorProjectName(anyString());

        assertThatThrownBy(() -> projectService.createProject(request, "x@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PROJECT_NAME);

        verifyNoInteractions(accountRepository, projectRepository, projectMemberRepository, projectMapper);
        verify(projectValidator).validatorProjectName("  ");
    }

    @Test
    void createProject_invalidEmail_throwsInvalidEmail() {
        CreateProjectRequest request = req("Test");

        when(accountRepository.findByEmail("no@test.com")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.createProject(request, "no@test.com"))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);

        verify(projectValidator).validatorProjectName("Test");
        verify(projectMapper, never()).mapToProject(any());
        verify(projectRepository, never()).save(any(Project.class));
        verify(projectMemberRepository, never()).save(any(ProjectMember.class));
    }

    @Test
    void createProject_success_setsKey_setsCreator_savesProject_andCreatesOwnerMember() {
        // given
        CreateProjectRequest request = req("Test Project Memos Final"); // -> prefix TPM
        String email = "creator@test.com";

        Account creator = new Account();
        creator.setId(UUID.randomUUID());
        creator.setEmail(email);

        Project mapped = new Project();
        mapped.setArchived(false);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(creator));
        when(projectMapper.mapToProject(request)).thenReturn(mapped);

        when(projectRepository.count()).thenReturn(0L); // nextNumber = 1

        // return saved project (simulate JPA assigns id)
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> {
            Project p = inv.getArgument(0);
            if (p.getId() == null) p.setId(UUID.randomUUID());
            return p;
        });

        // when
        projectService.createProject(request, email);

        // then
        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());

        Project savedProject = projectCaptor.getValue();
        assertThat(savedProject.getName()).isEqualTo(mapped.getName()); // mapped may not set name; depends on mapper
        assertThat(savedProject.getCreatedBy()).isSameAs(creator);
        assertThat(savedProject.getProjectKey()).isEqualTo("TPM-1");

        ArgumentCaptor<ProjectMember> memberCaptor = ArgumentCaptor.forClass(ProjectMember.class);
        verify(projectMemberRepository).save(memberCaptor.capture());

        ProjectMember owner = memberCaptor.getValue();
        assertThat(owner.getProject()).isSameAs(savedProject);
        assertThat(owner.getAccount()).isSameAs(creator);
        assertThat(owner.getRole()).isEqualTo(ProjectRole.OWNER);
        assertThat(owner.getInvitedEmail()).isEqualTo(email);
        assertThat(owner.getInvitedStatus()).isEqualTo(InviteStatus.ACCEPTED);
        assertThat(owner.getJoinedAt()).isNotNull();
    }

    @Test
    void createProject_generateKey_oneWord_usesFirst2Chars_andCountPlus1() {
        CreateProjectRequest request = req("Test"); // -> TE
        String email = "creator@test.com";

        Account creator = new Account();
        creator.setId(UUID.randomUUID());
        creator.setEmail(email);

        Project mapped = new Project();

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(creator));
        when(projectMapper.mapToProject(request)).thenReturn(mapped);

        when(projectRepository.count()).thenReturn(41L); // nextNumber = 42

        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        projectService.createProject(request, email);

        ArgumentCaptor<Project> projectCaptor = ArgumentCaptor.forClass(Project.class);
        verify(projectRepository).save(projectCaptor.capture());
        assertThat(projectCaptor.getValue().getProjectKey()).isEqualTo("TE-42");
    }

    @Test
    void deleteProject_notFound_throwsProjectNotFound() {
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.deleteProject(id))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository, never()).save(any());
    }

    @Test
    void deleteProject_alreadyArchived_throwsProjectAlreadyArchived() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        p.setId(id);
        p.setArchived(true);

        when(projectRepository.findById(id)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projectService.deleteProject(id))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_ALREADY_ARCHIVED);

        verify(projectRepository, never()).save(any());
    }

    @Test
    void deleteProject_success_setsArchivedTrue_andSaves() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        p.setId(id);
        p.setArchived(false);

        when(projectRepository.findById(id)).thenReturn(Optional.of(p));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        projectService.deleteProject(id);

        assertThat(p.isArchived()).isTrue();
        verify(projectRepository).save(p);
    }

    @Test
    void updateProject_notFound_throwsProjectNotFound() {
        UUID id = UUID.randomUUID();
        CreateProjectRequest request = req("New Name");

        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.updateProject(request, id))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository, never()).save(any());
        verify(projectMapper, never()).mapToCreateProjectResponse(any());
    }

    @Test
    void updateProject_archived_throwsProjectNotFound() {
        UUID id = UUID.randomUUID();
        CreateProjectRequest request = req("New Name");

        Project p = new Project();
        p.setId(id);
        p.setArchived(true);

        when(projectRepository.findById(id)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projectService.updateProject(request, id))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);

        verify(projectRepository, never()).save(any());
        verify(projectMapper, never()).mapToCreateProjectResponse(any());
    }

    @Test
    void updateProject_success_updatesFields_saves_andMapsResponse() {
        UUID id = UUID.randomUUID();
        CreateProjectRequest request = req("New Name");

        Project p = new Project();
        p.setId(id);
        p.setArchived(false);
        p.setName("Old");
        p.setImageUrl("oldImg");
        p.setDescription("oldDesc");

        when(projectRepository.findById(id)).thenReturn(Optional.of(p));
        when(projectRepository.save(any(Project.class))).thenAnswer(inv -> inv.getArgument(0));

        CreateProjectResponse mapped = mock(CreateProjectResponse.class);
        when(projectMapper.mapToCreateProjectResponse(p)).thenReturn(mapped);

        CreateProjectResponse res = projectService.updateProject(request, id);

        assertThat(p.getName()).isEqualTo("New Name");
        assertThat(p.getImageUrl()).isEqualTo("img");
        assertThat(p.getDescription()).isEqualTo("desc");
        assertThat(res).isSameAs(mapped);

        verify(projectValidator).validatorProjectName("New Name");
    }

    @Test
    void getProject_notFound_throwsProjectNotFound() {
        UUID id = UUID.randomUUID();
        when(projectRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProject(id))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    void getProject_archived_throwsProjectNotFound() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        p.setId(id);
        p.setArchived(true);

        when(projectRepository.findById(id)).thenReturn(Optional.of(p));

        assertThatThrownBy(() -> projectService.getProject(id))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.PROJECT_NOT_FOUND);
    }

    @Test
    void getProject_success_mapsResponse() {
        UUID id = UUID.randomUUID();
        Project p = new Project();
        p.setId(id);
        p.setArchived(false);

        when(projectRepository.findById(id)).thenReturn(Optional.of(p));

        CreateProjectResponse mapped = mock(CreateProjectResponse.class);
        when(projectMapper.mapToCreateProjectResponse(p)).thenReturn(mapped);

        CreateProjectResponse res = projectService.getProject(id);

        assertThat(res).isSameAs(mapped);
        verify(projectMapper).mapToCreateProjectResponse(p);
    }

    @Test
    void getProjects_admin_returnsAllNonArchivedMapped() {
        String email = "admin@test.com";

        when(userPermission.isAdminByEmail(email)).thenReturn(true);

        Project p1 = new Project(); p1.setArchived(false);
        Project p2 = new Project(); p2.setArchived(false);

        when(projectRepository.findByArchivedFalse()).thenReturn(List.of(p1, p2));

        CreateProjectResponse r1 = mock(CreateProjectResponse.class);
        CreateProjectResponse r2 = mock(CreateProjectResponse.class);
        when(projectMapper.mapToCreateProjectResponse(p1)).thenReturn(r1);
        when(projectMapper.mapToCreateProjectResponse(p2)).thenReturn(r2);

        List<CreateProjectResponse> res = projectService.getProjects(email);

        assertThat(res).containsExactly(r1, r2);
        verifyNoInteractions(accountRepository, projectMemberRepository);
    }

    @Test
    void getProjects_member_invalidEmail_throwsInvalidEmail() {
        String email = "member@test.com";
        when(userPermission.isAdminByEmail(email)).thenReturn(false);
        when(accountRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> projectService.getProjects(email))
                .isInstanceOf(AppException.class)
                .extracting(e -> ((AppException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_EMAIL);

        verify(projectMemberRepository, never()).findByAccountId(any());
    }

    @Test
    void getProjects_member_returnsNonArchivedProjectsMapped() {
        String email = "member@test.com";
        when(userPermission.isAdminByEmail(email)).thenReturn(false);

        Account acc = new Account();
        UUID accId = UUID.randomUUID();
        acc.setId(accId);
        acc.setEmail(email);

        when(accountRepository.findByEmail(email)).thenReturn(Optional.of(acc));

        Project active = new Project(); active.setArchived(false);
        Project archived = new Project(); archived.setArchived(true);

        ProjectMember m1 = new ProjectMember(); m1.setProject(active);
        ProjectMember m2 = new ProjectMember(); m2.setProject(archived);

        when(projectMemberRepository.findByAccountId(accId)).thenReturn(List.of(m1, m2));

        CreateProjectResponse mapped = mock(CreateProjectResponse.class);
        when(projectMapper.mapToCreateProjectResponse(active)).thenReturn(mapped);

        List<CreateProjectResponse> res = projectService.getProjects(email);

        assertThat(res).containsExactly(mapped);
        verify(projectMapper, times(1)).mapToCreateProjectResponse(active);
        verify(projectMapper, never()).mapToCreateProjectResponse(archived);
    }
}
