package com.namdang.memos.api;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.namdang.memos.BaseIntegrationTest;
import com.namdang.memos.dto.requests.auth.AccountSetupRequest;
import com.namdang.memos.dto.requests.auth.AuthenticationRequest;
import com.namdang.memos.dto.responses.auth.RegisterResponse;
import com.namdang.memos.dto.responses.auth.RegistrationResult;
import com.namdang.memos.dto.responses.auth.TokenPair;
import com.namdang.memos.enumType.AuthProvider;
import com.namdang.memos.service.OryAuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockCookie;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class AuthenticationApiIT extends BaseIntegrationTest {

    @Autowired ObjectMapper objectMapper;

    @MockBean OryAuthService oryAuthService;

    @Value("${ory.session.cookie.name}")
    String oryCookieName;

    private static MockCookie cookieFromSetCookieHeaders(List<String> setCookieHeaders, String cookieName) {
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

        List<String> registerSetCookies = registerRes.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        MockCookie refresh1 = cookieFromSetCookieHeaders(registerSetCookies, "refresh_token");

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
        List<String> loginSetCookies = loginRes.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        MockCookie refresh2 = cookieFromSetCookieHeaders(loginSetCookies, "refresh_token");

        // 3) ME
        mockMvc.perform(
                        get("/auth/me")
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.email").value(email));

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

        List<String> refreshSetCookies = refreshRes.getResponse().getHeaders(HttpHeaders.SET_COOKIE);
        MockCookie refresh3 = cookieFromSetCookieHeaders(refreshSetCookies, "refresh_token");

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

    @Test
    void auth_oidc_ory_setsRefreshCookie_andReturnsRegisterResponse() throws Exception {
        // given
        String oryCookieValue = "ory_cookie_value_test";

        TokenPair tokenPair = TokenPair.builder()
                .accessToken("access.xxx")
                .refreshToken("refresh.yyy")
                .refreshTtl(3600L)
                .build();

        RegisterResponse registerResponse = RegisterResponse.builder()
                .accessToken("access.xxx")
                .role("MEMBER")
                .permissions(Set.of("PROJECT.CREATE"))
                .provider(AuthProvider.GOOGLE)
                .build();

        RegistrationResult result = RegistrationResult.builder()
                .tokenPair(tokenPair)
                .registerResponse(registerResponse)
                .build();

        when(oryAuthService.loginFromOrySession(eq(oryCookieName), eq(oryCookieValue)))
                .thenReturn(result);

        // when
        var res = mockMvc.perform(
                        post("/auth/oidc/ory")
                                .cookie(new MockCookie(oryCookieName, oryCookieValue))
                )
                .andExpect(status().isOk())
                .andExpect(header().exists(HttpHeaders.SET_COOKIE))
                .andExpect(jsonPath("$.result").exists())
                .andExpect(jsonPath("$.result.role").value("MEMBER"))
                .andReturn();

        // then: cookie flags
        String setCookie = res.getResponse().getHeader(HttpHeaders.SET_COOKIE);
        assertThat(setCookie).contains("refresh_token=refresh.yyy");
        assertThat(setCookie).contains("HttpOnly");
        assertThat(setCookie).contains("Secure");
        assertThat(setCookie).contains("SameSite=None");
        assertThat(setCookie).contains("Path=/auth");

        verify(oryAuthService).loginFromOrySession(oryCookieName, oryCookieValue);
    }
}
