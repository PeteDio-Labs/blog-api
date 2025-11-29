package com.petedillo.api.controller;

import com.petedillo.api.config.AppConfig;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.BlogTag;
import com.petedillo.api.service.BlogPostService;
import com.petedillo.api.service.MediaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AdminController.class)
@ActiveProfiles("test")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BlogPostService blogPostService;

    @MockitoBean
    private MediaService mediaService;

    @MockitoBean
    private AppConfig appConfig;

    private BlogPost testPost;
    private BlogTag testTag;

    @BeforeEach
    void setUp() {
        testPost = new BlogPost();
        testPost.setId(1L);
        testPost.setTitle("Test Post");
        testPost.setSlug("test-post");
        testPost.setContent("Test content");
        testPost.setExcerpt("Test excerpt");
        testPost.setStatus("published");
        testPost.setPublishedAt(LocalDateTime.now());

        testTag = new BlogTag();
        testTag.setId(1L);
        testTag.setTagName("java");
    }

    // === Login Page Tests ===

    @Test
    void testLoginPage_NoParams_RendersLoginTemplate() throws Exception {
        mockMvc.perform(get("/manage/login"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/login"));
    }

    @Test
    void testLoginPage_WithError_ShowsErrorMessage() throws Exception {
        mockMvc.perform(get("/manage/login").param("error", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/login"))
            .andExpect(model().attribute("error", "Invalid username or password"));
    }

    @Test
    void testLoginPage_WithLogout_ShowsLogoutMessage() throws Exception {
        mockMvc.perform(get("/manage/login").param("logout", "true"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/login"))
            .andExpect(model().attribute("message", "You have been logged out successfully"));
    }

    // === Posts List Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPostsList_NoFilters_ShowsAllPosts() throws Exception {
        List<BlogPost> posts = Arrays.asList(testPost);
        when(blogPostService.getAllPosts()).thenReturn(posts);
        when(blogPostService.getAllTags()).thenReturn(Arrays.asList(testTag));

        mockMvc.perform(get("/manage/posts"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/posts/list"))
            .andExpect(model().attribute("posts", posts))
            .andExpect(model().attribute("allTags", hasSize(1)))
            .andExpect(model().attribute("pageTitle", "Manage Posts"));

        verify(blogPostService).getAllPosts();
        verify(blogPostService).getAllTags();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPostsList_WithSearchQuery_FiltersPostsBySearch() throws Exception {
        List<BlogPost> searchResults = Arrays.asList(testPost);
        when(blogPostService.searchPosts("test")).thenReturn(searchResults);
        when(blogPostService.getAllTags()).thenReturn(Arrays.asList(testTag));

        mockMvc.perform(get("/manage/posts").param("search", "test"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/posts/list"))
            .andExpect(model().attribute("posts", searchResults))
            .andExpect(model().attribute("search", "test"));

        verify(blogPostService).searchPosts("test");
        verify(blogPostService, never()).getAllPosts();
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testPostsList_WithTagFilter_FiltersPostsByTag() throws Exception {
        List<BlogPost> taggedPosts = Arrays.asList(testPost);
        when(blogPostService.getPostsByTag("java")).thenReturn(taggedPosts);
        when(blogPostService.getAllTags()).thenReturn(Arrays.asList(testTag));

        mockMvc.perform(get("/manage/posts").param("tag", "java"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/posts/list"))
            .andExpect(model().attribute("posts", taggedPosts))
            .andExpect(model().attribute("selectedTag", "java"));

        verify(blogPostService).getPostsByTag("java");
    }

    // === New Post Form Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testNewPostForm_RendersEmptyForm() throws Exception {
        when(blogPostService.getAllTags()).thenReturn(Arrays.asList(testTag));

        mockMvc.perform(get("/manage/posts/new"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/posts/form"))
            .andExpect(model().attributeExists("post"))
            .andExpect(model().attribute("allTags", hasSize(1)))
            .andExpect(model().attribute("pageTitle", "Create New Post"))
            .andExpect(model().attribute("isEdit", false));
    }

    // === Edit Post Form Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testEditPostForm_ValidId_RendersFormWithPost() throws Exception {
        when(blogPostService.getPostById(1L)).thenReturn(testPost);
        when(blogPostService.getAllTags()).thenReturn(Arrays.asList(testTag));
        when(mediaService.getMediaForPost(1L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/manage/posts/1"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/posts/form"))
            .andExpect(model().attribute("post", testPost))
            .andExpect(model().attribute("pageTitle", "Edit Post: Test Post"))
            .andExpect(model().attribute("isEdit", true))
            .andExpect(model().attributeExists("media"));

        verify(blogPostService).getPostById(1L);
        verify(mediaService).getMediaForPost(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testEditPostForm_InvalidId_RedirectsWithError() throws Exception {
        when(blogPostService.getPostById(999L)).thenReturn(null);

        mockMvc.perform(get("/manage/posts/999"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts?error=notfound"));
    }

    // === Create Post Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreatePost_ValidData_RedirectsToEditPage() throws Exception {
        BlogPost createdPost = new BlogPost();
        createdPost.setId(2L);
        createdPost.setTitle("New Post");

        when(blogPostService.createPost(
            eq("New Post"),
            eq("Content"),
            eq("Excerpt"),
            eq("draft"),
            anySet()
        )).thenReturn(createdPost);

        mockMvc.perform(post("/manage/posts")
                .with(csrf())
                .param("title", "New Post")
                .param("content", "Content")
                .param("excerpt", "Excerpt")
                .param("status", "draft"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts/2"))
            .andExpect(flash().attribute("success", "Post created successfully"));

        verify(blogPostService).createPost(eq("New Post"), eq("Content"), eq("Excerpt"), eq("draft"), anySet());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreatePost_WithTags_CreatesPostWithTags() throws Exception {
        BlogPost createdPost = new BlogPost();
        createdPost.setId(2L);

        when(blogPostService.createPost(anyString(), anyString(), anyString(), anyString(), anySet()))
            .thenReturn(createdPost);

        mockMvc.perform(post("/manage/posts")
                .with(csrf())
                .param("title", "Post with tags")
                .param("content", "Content")
                .param("status", "draft")
                .param("tags", "java")
                .param("tags", "spring"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts/2"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testCreatePost_ServiceThrowsException_RedirectsWithError() throws Exception {
        when(blogPostService.createPost(anyString(), anyString(), anyString(), anyString(), anySet()))
            .thenThrow(new RuntimeException("Database error"));

        mockMvc.perform(post("/manage/posts")
                .with(csrf())
                .param("title", "New Post")
                .param("content", "Content")
                .param("status", "draft"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts/new"))
            .andExpect(flash().attribute("error", containsString("Failed to create post")));
    }

    // === Update Post Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdatePost_ValidData_RedirectsToEditPage() throws Exception {
        when(blogPostService.updatePost(
            eq(1L),
            eq("Updated Title"),
            eq("Updated Content"),
            eq("Updated Excerpt"),
            eq("published"),
            anySet()
        )).thenReturn(testPost);

        mockMvc.perform(post("/manage/posts/1")
                .with(csrf())
                .param("title", "Updated Title")
                .param("content", "Updated Content")
                .param("excerpt", "Updated Excerpt")
                .param("status", "published"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts/1"))
            .andExpect(flash().attribute("success", "Post updated successfully"));

        verify(blogPostService).updatePost(eq(1L), anyString(), anyString(), anyString(), anyString(), anySet());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testUpdatePost_ServiceThrowsException_RedirectsWithError() throws Exception {
        when(blogPostService.updatePost(anyLong(), anyString(), anyString(), anyString(), anyString(), anySet()))
            .thenThrow(new RuntimeException("Update failed"));

        mockMvc.perform(post("/manage/posts/1")
                .with(csrf())
                .param("title", "Title")
                .param("content", "Content")
                .param("status", "draft"))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts/1"))
            .andExpect(flash().attribute("error", containsString("Failed to update post")));
    }

    // === Delete Post Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeletePost_ValidId_RedirectsToPostsList() throws Exception {
        doNothing().when(blogPostService).deletePost(1L);

        mockMvc.perform(post("/manage/posts/1/delete")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts"))
            .andExpect(flash().attribute("success", "Post deleted successfully"));

        verify(blogPostService).deletePost(1L);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testDeletePost_ServiceThrowsException_RedirectsWithError() throws Exception {
        doThrow(new RuntimeException("Delete failed")).when(blogPostService).deletePost(1L);

        mockMvc.perform(post("/manage/posts/1/delete")
                .with(csrf()))
            .andExpect(status().is3xxRedirection())
            .andExpect(redirectedUrl("/manage/posts"))
            .andExpect(flash().attribute("error", containsString("Failed to delete post")));
    }

    // === Media Manager Tests ===

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testMediaManager_RendersWithAllMedia() throws Exception {
        when(mediaService.getAllMediaEntities()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/manage/media"))
            .andExpect(status().isOk())
            .andExpect(view().name("admin/media/manager"))
            .andExpect(model().attributeExists("media"))
            .andExpect(model().attribute("pageTitle", "Media Manager"));

        verify(mediaService).getAllMediaEntities();
    }

    // === Security Tests ===

    @Test
    void testPostsList_UnauthenticatedUser_RedirectsToLogin() throws Exception {
        mockMvc.perform(get("/manage/posts"))
            .andExpect(status().is3xxRedirection());
    }

    @Test
    void testCreatePost_WithoutCsrf_ReturnsForbidden() throws Exception {
        mockMvc.perform(post("/manage/posts")
                .param("title", "Test"))
            .andExpect(status().isForbidden());
    }
}
