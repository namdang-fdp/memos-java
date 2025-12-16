package com.namdang.memos.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdang.memos.BaseIntegrationTest;
import com.namdang.memos.dto.requests.auth.AccountSetupRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationApiIT extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    private static MockCookie cookieFromSetCookie(String setCookieHeader, String cookieName) {
        String first = setCookieHeader.split(";", 2)[0];
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

    @Test
    void auth_fullFlow_register_login_me_profileSetup_refresh_logout() throws Exception {
        String email = "it_" + System.currentTimeMillis() + "@test.com";
        String password = "123456";

        // 1) REGISTER
        AuthenticationRequest registerReq =
                AuthenticationRequest.builder().email(email).password(password).build();

        var registerRes = mockMvc.perform(
                        post("/auth/register")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(registerReq))
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.result").exists())
                .andReturn();

        String registerSetCookie = registerRes.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(registerSetCookie).contains("refresh_token=");
        MockCookie refresh1 = cookieFromSetCookie(registerSetCookie, "refresh_token");

        // 2) LOGIN
        AuthenticationRequest loginReq =
                AuthenticationRequest.builder().email(email).password(password).build();

        var loginRes = mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginReq))
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.result.authenticated").value(true))
                .andExpect(jsonPath("$.result.token").isNotEmpty())
                .andReturn();

        String accessToken = extractAccessToken(loginRes.getResponse().getContentAsString());
        String loginSetCookie = loginRes.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        MockCookie refresh2 = cookieFromSetCookie(loginSetCookie, "refresh_token");

        // 3) ME
        mockMvc.perform(
                        get("/auth/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.email").value(email)); // nếu MeResponse có field email

        // 4) PROFILE SETUP
        AccountSetupRequest setupReq = AccountSetupRequest.builder().name("New Name IT").build();

        mockMvc.perform(
                        post("/auth/profile/setup")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(setupReq))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists());

        // 5) REFRESH
        var refreshRes = mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(refresh2)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.result.authenticated").value(true))
                .andExpect(jsonPath("$.result.token").isNotEmpty())
                .andReturn();

        String refreshedAccessToken = extractAccessToken(refreshRes.getResponse().getContentAsString());
        assertThat(refreshedAccessToken).isNotBlank();

        String refreshSetCookie = refreshRes.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        MockCookie refresh3 = cookieFromSetCookie(refreshSetCookie, "refresh_token");

        // 6) LOGOUT
        mockMvc.perform(
                        post("/auth/logout")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + refreshedAccessToken)
                                .cookie(refresh3)
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE));

        // call /auth/refresh by old token (invalidate token) --> fail
        mockMvc.perform(
                        post("/auth/refresh")
                                .cookie(refresh2)
                )
                .andExpect(status().is4xxClientError());
    }
}
