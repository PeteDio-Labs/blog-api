package com.petedillo.api.security;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.authenticated;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityConfigTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void whenAccessingManagePostsWithoutAuth_thenRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/manage/posts"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/manage/login"));
    }

    @Test
    void whenAccessingLoginPage_thenAllowsAccess() throws Exception {
        mockMvc.perform(get("/manage/login"))
                .andExpect(status().isOk());
    }

    @Test
    void whenLoginWithValidCredentials_thenRedirectsToPosts() throws Exception {
        mockMvc.perform(formLogin("/manage/login")
                        .user("admin")
                        .password("admin123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manage/posts"))
                .andExpect(authenticated().withUsername("admin"));
    }

    @Test
    void whenLoginWithInvalidCredentials_thenRedirectsToLoginWithError() throws Exception {
        mockMvc.perform(formLogin("/manage/login")
                        .user("admin")
                        .password("wrongpassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manage/login?error=true"))
                .andExpect(unauthenticated());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenAuthenticatedAndAccessingManagePosts_thenAllowsAccess() throws Exception {
        mockMvc.perform(get("/manage/posts"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenAuthenticatedAndLoggingOut_thenRedirectsToLoginWithLogout() throws Exception {
        mockMvc.perform(post("/manage/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/manage/login?logout=true"));
    }

    @Test
    void whenAccessingPublicApiWithoutAuth_thenAllowsAccess() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessingHealthEndpointWithoutAuth_thenAllowsAccess() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessingActuatorHealthLivenessWithoutAuth_thenAllowsAccess() throws Exception {
        mockMvc.perform(get("/actuator/health/liveness"))
                .andExpect(status().isOk());
    }

    @Test
    void whenAccessingActuatorHealthReadinessWithoutAuth_thenAllowsAccess() throws Exception {
        mockMvc.perform(get("/actuator/health/readiness"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenSubmittingFormWithCsrf_thenAllowsAccess() throws Exception {
        mockMvc.perform(post("/manage/posts").with(csrf())
                        .param("title", "Test Post")
                        .param("content", "Test Content"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenSubmittingFormWithoutCsrf_thenForbidden() throws Exception {
        mockMvc.perform(post("/manage/posts")
                        .param("title", "Test Post")
                        .param("content", "Test Content"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void whenAccessingManageApiWithoutCsrf_thenAllowsAccess() throws Exception {
        // AJAX endpoints should have CSRF disabled
        mockMvc.perform(post("/manage/api/media/upload")
                        .contentType("multipart/form-data"))
                .andExpect(status().isBadRequest()); // Validation error, not CSRF error (403)
    }
}
