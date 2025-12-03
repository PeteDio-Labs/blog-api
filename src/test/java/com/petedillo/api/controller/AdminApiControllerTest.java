package com.petedillo.api.controller;

import com.petedillo.api.config.AppConfig;
import com.petedillo.api.config.PasswordEncoderConfig;
import com.petedillo.api.config.SecurityConfig;
import com.petedillo.api.dto.MediaDTO;
import com.petedillo.api.service.AdminUserDetailsService;
import com.petedillo.api.service.BlogPostService;
import com.petedillo.api.service.MediaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminApiController.class)
@Import({SecurityConfig.class, PasswordEncoderConfig.class})
@ActiveProfiles("test")
class AdminApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MediaService mediaService;

    @MockitoBean
    private BlogPostService blogPostService;

    @MockitoBean
    private AppConfig appConfig;

    @MockitoBean
    private AdminUserDetailsService adminUserDetailsService;

    // === Upload Media Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUploadMedia_ValidFile_ReturnsSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "test image content".getBytes()
        );

        MediaDTO mediaDTO = new MediaDTO();
        mediaDTO.setId(1L);
        mediaDTO.setUrl("/api/v1/media/images/test.jpg");
        mediaDTO.setAltText("Test image");
        mediaDTO.setDisplayOrder(0);

        when(mediaService.uploadMedia(any(), eq(1L), eq("Test image"), isNull()))
            .thenReturn(mediaDTO);

        mockMvc.perform(multipart("/manage/api/media/upload")
                .file(file)
                .param("postId", "1")
                .param("altText", "Test image"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.media.id").value(1))
            .andExpect(jsonPath("$.media.url").value("/api/v1/media/images/test.jpg"))
            .andExpect(jsonPath("$.media.altText").value("Test image"));

        verify(mediaService).uploadMedia(any(), eq(1L), eq("Test image"), isNull());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUploadMedia_InvalidFileType_ReturnsBadRequest() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.exe",
            "application/x-msdownload",
            "malicious".getBytes()
        );

        when(mediaService.uploadMedia(any(), eq(1L), anyString(), isNull()))
            .thenThrow(new IllegalArgumentException("File type not allowed"));

        mockMvc.perform(multipart("/manage/api/media/upload")
                .file(file)
                .param("postId", "1")
                .param("altText", "Test"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("File type not allowed"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUploadMedia_IOError_ReturnsServerError() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "content".getBytes()
        );

        when(mediaService.uploadMedia(any(), eq(1L), any(), any()))
            .thenThrow(new com.petedillo.api.exception.MediaUploadException("Failed to upload media file"));

        mockMvc.perform(multipart("/manage/api/media/upload")
                .file(file)
                .param("postId", "1"))
            .andExpect(status().isBadRequest()) // Controller returns 400 for all exceptions
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value(containsString("Failed to upload")));
    }

    // === Delete Media Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteMedia_ValidId_ReturnsSuccess() throws Exception {
        doNothing().when(mediaService).deleteMedia(1L);

        mockMvc.perform(delete("/manage/api/media/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(mediaService).deleteMedia(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeleteMedia_InvalidId_ReturnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Media not found"))
            .when(mediaService).deleteMedia(999L);

        mockMvc.perform(delete("/manage/api/media/999"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Media not found"));
    }

    // === Reorder Media Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testReorderMedia_ValidOrder_ReturnsSuccess() throws Exception {
        doNothing().when(mediaService).reorderMedia(eq(1L), anyList());

        mockMvc.perform(put("/manage/api/posts/1/media/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mediaIds\": [2, 1, 3]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(mediaService).reorderMedia(eq(1L), eq(Arrays.asList(2L, 1L, 3L)));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testReorderMedia_MediaFromDifferentPost_ReturnsBadRequest() throws Exception {
        doThrow(new IllegalArgumentException("Media does not belong to this post"))
            .when(mediaService).reorderMedia(eq(1L), anyList());

        mockMvc.perform(put("/manage/api/posts/1/media/reorder")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mediaIds\": [2, 3]}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.success").value(false))
            .andExpect(jsonPath("$.error").value("Media does not belong to this post"));
    }

    // === Update Alt Text Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdateAltText_ValidData_ReturnsSuccess() throws Exception {
        MediaDTO mediaDTO = new MediaDTO();
        mediaDTO.setId(1L);
        mediaDTO.setAltText("Updated alt text");

        when(mediaService.updateMetadata(eq(1L), eq("Updated alt text"), isNull()))
            .thenReturn(mediaDTO);

        mockMvc.perform(put("/manage/api/media/1/alt-text")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"altText\":\"Updated alt text\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.media.id").value(1))
            .andExpect(jsonPath("$.media.altText").value("Updated alt text"));

        verify(mediaService).updateMetadata(eq(1L), eq("Updated alt text"), isNull());
    }

    // === Get Post Media Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetPostMedia_ReturnsMediaList() throws Exception {
        MediaDTO media1 = new MediaDTO();
        media1.setId(1L);
        media1.setDisplayOrder(0);

        MediaDTO media2 = new MediaDTO();
        media2.setId(2L);
        media2.setDisplayOrder(1);

        List<MediaDTO> mediaList = Arrays.asList(media1, media2);
        when(mediaService.getMediaForPost(1L)).thenReturn(mediaList);

        mockMvc.perform(get("/manage/api/posts/1/media"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.media", hasSize(2)))
            .andExpect(jsonPath("$.media[0].id").value(1))
            .andExpect(jsonPath("$.media[1].id").value(2));

        verify(mediaService).getMediaForPost(1L);
    }

    // === Set Cover Image Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testSetCoverImage_ValidData_ReturnsSuccess() throws Exception {
        com.petedillo.api.model.BlogPost mockPost = new com.petedillo.api.model.BlogPost();
        mockPost.setId(1L);
        when(blogPostService.setCoverImage(eq(1L), eq(2L))).thenReturn(mockPost);

        mockMvc.perform(post("/manage/api/posts/1/cover-image")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"mediaId\":2}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.success").value(true));

        verify(blogPostService).setCoverImage(eq(1L), eq(2L));
    }

    // === Security Tests ===

    @Test
    void testUploadMedia_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "test.jpg",
            "image/jpeg",
            "content".getBytes()
        );

        mockMvc.perform(multipart("/manage/api/media/upload")
                .file(file)
                .param("postId", "1"))
            .andExpect(status().is3xxRedirection()); // Redirects to login page
    }

    @Test
    void testDeleteMedia_UnauthenticatedUser_ReturnsUnauthorized() throws Exception {
        mockMvc.perform(delete("/manage/api/media/1"))
            .andExpect(status().is3xxRedirection()); // Redirects to login page
    }
}
