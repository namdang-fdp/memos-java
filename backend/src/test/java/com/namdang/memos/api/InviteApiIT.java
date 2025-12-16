package com.namdang.memos.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdang.memos.BaseIntegrationTest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.requests.invite.ProjectInviteRequest;
import com.namdang.memos.dto.requests.project.CreateProjectRequest;
import com.namdang.memos.entity.ProjectMember;
import com.namdang.memos.enumType.InviteStatus;
import com.namdang.memos.repository.ProjectMemberRepository;
import com.namdang.memos.service.MailService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class InviteApiIT extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;
    @Autowired ProjectMemberRepository projectMemberRepository;

    // IMPORTANT: avoid real SMTP / MailHog dependency in CI
    @MockBean MailService mailService;

    private static String extractAccessToken(String responseBody) throws Exception {
        JsonNode root = new ObjectMapper().readTree(responseBody);
        return root.path("result").path("token").asText();
    }

    private String registerAndLoginGetAccessToken(String email, String password) throws Exception {
        // register
        AuthenticationRequest registerReq = AuthenticationRequest.builder()
                .email(email)
                .password(password)
                .build();

        mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerReq))
                )
                .andExpect(status().isOk());

        // login
        AuthenticationRequest loginReq = AuthenticationRequest.builder()
                .email(email)
                .password(password)
                .build();

        var loginRes = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result.authenticated").value(true))
                .andExpect(jsonPath("$.result.token").isNotEmpty())
                .andReturn();

        return extractAccessToken(loginRes.getResponse().getContentAsString());
    }

    private UUID createProjectAndGetId(String accessToken, String projectName) throws Exception {
        CreateProjectRequest req = new CreateProjectRequest();
        req.setName(projectName);
        req.setImageUrl("img");
        req.setDescription("desc");

        // create project
        mockMvc.perform(
                        post("/project")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req))
                )
                .andExpect(status().isOk());

        // list projects and find id by name
        var listRes = mockMvc.perform(
                        get("/projects")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andReturn();

        JsonNode root = objectMapper.readTree(listRes.getResponse().getContentAsString());
        for (JsonNode p : root.path("result")) {
            if (projectName.equals(p.path("name").asText())) {
                return UUID.fromString(p.path("id").asText());
            }
        }
        throw new AssertionError("Project not found by name in /projects response: " + projectName);
    }

    private String findInviteToken(UUID projectId, String invitedEmail) {
        // repo method name in your codebase: existsByProject_IdAndInvitedEmailAndInvitedStatus(...)
        // but we need the token -> easiest is query all by token search.
        // If you don't have a query method, we can scan by inviteToken non-null.
        Optional<ProjectMember> pm = projectMemberRepository.findAll().stream()
                .filter(m -> m.getProject() != null && projectId.equals(m.getProject().getId()))
                .filter(m -> invitedEmail.equalsIgnoreCase(m.getInvitedEmail()))
                .filter(m -> m.getInvitedStatus() == InviteStatus.PENDING)
                .filter(m -> m.getInviteToken() != null && !m.getInviteToken().isBlank())
                .findFirst();

        ProjectMember found = pm.orElseThrow(() ->
                new AssertionError("Invite token not found in DB for invitedEmail=" + invitedEmail));

        return found.getInviteToken();
    }

    @Test
    void invite_fullFlow_ownerCreatesInvite_publicInfo_accept_decline_andNonOwnerForbidden() throws Exception {
        String pass = "123456";

        // ===== owner =====
        String ownerEmail = "it_owner_" + System.currentTimeMillis() + "@test.com";
        String ownerToken = registerAndLoginGetAccessToken(ownerEmail, pass);

        UUID projectId = createProjectAndGetId(ownerToken, "Invite Project " + System.currentTimeMillis());

        // ===== invited user (existing account) =====
        String invitedEmail = "it_invited_" + System.currentTimeMillis() + "@test.com";
        String invitedToken = registerAndLoginGetAccessToken(invitedEmail, pass);

        // ===== non-owner =====
        String otherEmail = "it_other_" + System.currentTimeMillis() + "@test.com";
        String otherToken = registerAndLoginGetAccessToken(otherEmail, pass);

        // 1) OWNER create invite (should pass projectPermission.canInviteToProject)
        ProjectInviteRequest inviteReq = new ProjectInviteRequest();
        inviteReq.setTargetUserEmail(invitedEmail);

        mockMvc.perform(
                        post("/invite/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inviteReq))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        // verify MailService called (mocked)
        verify(mailService, times(1)).sendProjectInviteMail(
                eq(invitedEmail),
                anyString(),          // project name
                eq(ownerEmail),       // inviterName uses owner email in service
                anyString()           // token generated
        );

        // fetch token from DB
        String token = findInviteToken(projectId, invitedEmail);
        assertThat(token).isNotBlank();

        // 2) non-owner tries create invite -> 403
        ProjectInviteRequest inviteReq2 = new ProjectInviteRequest();
        inviteReq2.setTargetUserEmail("someone_" + System.currentTimeMillis() + "@test.com");

        mockMvc.perform(
                        post("/invite/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + otherToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inviteReq2))
                )
                .andExpect(status().isForbidden());

        // 3) public invite info (no auth required)
        mockMvc.perform(
                        get("/invite/info/{token}", token)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        // 4) invited user accepts
        mockMvc.perform(
                        post("/invite/{token}/accept", token)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitedToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        // 5) create another invite then decline (to cover decline endpoint)
        String invitedEmail2 = "it_invited2_" + System.currentTimeMillis() + "@test.com";
        String invitedToken2 = registerAndLoginGetAccessToken(invitedEmail2, pass);

        ProjectInviteRequest inviteReq3 = new ProjectInviteRequest();
        inviteReq3.setTargetUserEmail(invitedEmail2);

        mockMvc.perform(
                        post("/invite/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + ownerToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(inviteReq3))
                )
                .andExpect(status().isOk());

        String token2 = findInviteToken(projectId, invitedEmail2);

        mockMvc.perform(
                        post("/invite/{token}/decline", token2)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + invitedToken2)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Decline invite successfully"));
    }
}
