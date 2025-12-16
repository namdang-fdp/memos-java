package com.namdang.memos.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdang.memos.BaseIntegrationTest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.requests.project.CreateProjectRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ProjectApiIT extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    private static MockCookie cookieFromSetCookieHeader(List<String> setCookieHeaders, String cookieName) {
        String header = setCookieHeaders.stream()
                .filter(h -> h != null && h.startsWith(cookieName + "="))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Missing Set-Cookie for: " + cookieName));

        String first = header.split(";", 2)[0];
        String[] kv = first.split("=", 2);
        String name = kv[0];
        String value = kv.length > 1 ? kv[1] : "";
        assertThat(name).isEqualTo(cookieName);

        return new MockCookie(cookieName, value);
    }

    private static String extractAccessToken(String responseBody) throws Exception {
        JsonNode root = new ObjectMapper().readTree(responseBody);
        return root.path("result").path("token").asText();
    }

    private static UUID extractProjectIdByName(String responseBody, String expectedName) throws Exception {
        JsonNode root = new ObjectMapper().readTree(responseBody);
        JsonNode arr = root.path("result");
        assertThat(arr.isArray()).isTrue();

        Optional<JsonNode> match = Optional.empty();
        for (JsonNode item : arr) {
            if (expectedName.equals(item.path("name").asText())) {
                match = Optional.of(item);
                break;
            }
        }

        JsonNode found = match.orElseThrow(() ->
                new AssertionError("Project not found in /projects response. expectedName=" + expectedName));

        String idText = found.path("id").asText(null);
        assertThat(idText).isNotBlank();
        return UUID.fromString(idText);
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

    @Test
    void project_fullFlow_ownerCanCRUD_andNonMemberForbidden() throws Exception {
        // ===== user1 (owner) =====
        String email1 = "it_owner_" + System.currentTimeMillis() + "@test.com";
        String pass = "123456";
        String access1 = registerAndLoginGetAccessToken(email1, pass);

        // 1) CREATE PROJECT
        String projectName = "IT Project " + System.currentTimeMillis();

        CreateProjectRequest createReq = new CreateProjectRequest();
        createReq.setName(projectName);
        createReq.setImageUrl("img");
        createReq.setDescription("desc");

        mockMvc.perform(
                        post("/project")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(createReq))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Create Project Successfully"));

        // 2) LIST PROJECTS -> find created id
        var listRes = mockMvc.perform(
                        get("/projects")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access1)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").isArray())
                .andReturn();

        UUID projectId = extractProjectIdByName(listRes.getResponse().getContentAsString(), projectName);

        // 3) GET PROJECT (owner allowed by ProjectPermission)
        mockMvc.perform(
                        get("/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access1)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.id").value(projectId.toString()));

        // 4) UPDATE PROJECT (owner allowed)
        CreateProjectRequest updateReq = new CreateProjectRequest();
        updateReq.setName(projectName + " Updated");
        updateReq.setImageUrl("img2");
        updateReq.setDescription("desc2");

        mockMvc.perform(
                        put("/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access1)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateReq))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.id").value(projectId.toString()))
                .andExpect(jsonPath("$.result.name").value(projectName + " Updated"));

        // 5) DELETE PROJECT (owner allowed by canDeleteProject)
        mockMvc.perform(
                        delete("/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access1)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").value("Delete Project Successfully"));

        // (optional sanity) GET after delete -> should be 4xx (archived -> service throws PROJECT_NOT_FOUND)
        mockMvc.perform(
                        get("/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access1)
                )
                .andExpect(status().is4xxClientError());

        // ===== user2 (non-member) =====
        String email2 = "it_user_" + System.currentTimeMillis() + "@test.com";
        String access2 = registerAndLoginGetAccessToken(email2, pass);

        // Non-member tries GET -> should be 403 due to @projectPermission.canViewAndUpdateProject
        mockMvc.perform(
                        get("/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access2)
                )
                .andExpect(status().isForbidden());

        // Non-member tries UPDATE -> 403
        mockMvc.perform(
                        put("/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access2)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateReq))
                )
                .andExpect(status().isForbidden());

        // Non-member tries DELETE -> 403
        mockMvc.perform(
                        delete("/project/{id}", projectId)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + access2)
                )
                .andExpect(status().isForbidden());
    }
}
