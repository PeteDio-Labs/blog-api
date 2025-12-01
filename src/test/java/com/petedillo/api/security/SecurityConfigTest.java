package com.petedillo.api.security;

import com.petedillo.api.service.BlogPostService;
import com.petedillo.api.service.FileStorageService;
import com.petedillo.api.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
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

    @MockitoBean
    private BlogPostService blogPostService;

    @MockitoBean
    private MediaService mediaService;

    @MockitoBean
    private FileStorageService fileStorageService;

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
        // Public API endpoints should be accessible without authentication
        // Note: May return empty list if no posts exist, but should not return 401/403
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
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
                .andExpect(status().is3xxRedirection()); // Redirects after successful create
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
        // AJAX/API endpoints under /manage/api should work without CSRF
        // The endpoint expects multipart, so we get 500, but the important thing is NOT 403 (CSRF error)
        mockMvc.perform(post("/manage/api/media/upload"))
                .andExpect(status().isInternalServerError()); // Multipart error, not CSRF forbidden (403)
    }
}
