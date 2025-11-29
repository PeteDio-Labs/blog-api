package com.petedillo.api.controller;

import com.petedillo.api.config.AppConfig;
import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.service.BlogPostService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ApiController.class)
class ApiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private BlogPostService blogPostService;

    @MockBean
    private DataSource dataSource;

    @MockBean
    private AppConfig appConfig;

    private BlogPost testPost;

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
        testPost.setTags(Arrays.asList("java", "spring-boot"));
        
        // Add test media
        BlogMedia coverImage = new BlogMedia();
        coverImage.setId(1L);
        coverImage.setBlogPost(testPost);
        coverImage.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        coverImage.setExternalUrl("https://example.com/cover.jpg");
        coverImage.setDisplayOrder(0);
        coverImage.setAltText("Test cover image");
        coverImage.setCaption("Test caption");
        testPost.getMedia().add(coverImage);

        // Mock AppConfig
        when(appConfig.getEnvironment()).thenReturn("dev");
        when(appConfig.getVersion()).thenReturn("1.0.0");
    }

    @Test
    void testHealthCheck_DatabaseConnected_ReturnsOk() throws Exception {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(true);
        when(blogPostService.getAllPosts()).thenReturn(Arrays.asList(testPost));

        // Act & Assert
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.environment").value("dev"))
            .andExpect(jsonPath("$.version").value("1.0.0"))
            .andExpect(jsonPath("$.database").value("connected"))
            .andExpect(jsonPath("$.postsCount").value(1));

        verify(mockConnection).close();
    }

    @Test
    void testHealthCheck_DatabaseDisconnected_ReturnsServiceUnavailable() throws Exception {
        // Arrange
        Connection mockConnection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(mockConnection);
        when(mockConnection.isValid(2)).thenReturn(false);

        // Act & Assert
        mockMvc.perform(get("/api/v1/health"))
            .andExpect(status().isServiceUnavailable())
            .andExpect(jsonPath("$.status").value("UP"))
            .andExpect(jsonPath("$.environment").value("dev"))
            .andExpect(jsonPath("$.database").value("disconnected"))
            .andExpect(jsonPath("$.error").value("Invalid connection"));

        verify(mockConnection).close();
    }

    @Test
    void testGetInfo_ReturnsEnvironmentInfo() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/info"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.environment").value("dev"))
            .andExpect(jsonPath("$.version").value("1.0.0"))
            .andExpect(jsonPath("$.name").value("Blog API"));
    }

    @Test
    void testGetAllPosts_ReturnsOkWithPosts() throws Exception {
        // Arrange
        List<BlogPost> posts = Arrays.asList(testPost);
        when(blogPostService.getAllPosts()).thenReturn(posts);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].title").value("Test Post"))
            .andExpect(jsonPath("$[0].slug").value("test-post"))
            .andExpect(jsonPath("$[0].tags", hasSize(2)))
            .andExpect(jsonPath("$[0].tags[0]").value("java"))
            .andExpect(jsonPath("$[0].tags[1]").value("spring-boot"))
            .andExpect(jsonPath("$[0].media", hasSize(1)))
            .andExpect(jsonPath("$[0].media[0].type").value("EXTERNAL_IMAGE"))
            .andExpect(jsonPath("$[0].media[0].url").value("https://example.com/cover.jpg"))
            .andExpect(jsonPath("$[0].coverImage.url").value("https://example.com/cover.jpg"))
            .andExpect(jsonPath("$[0].coverImage.altText").value("Test cover image"));

        verify(blogPostService).getAllPosts();
    }

    @Test
    void testGetAllPosts_EmptyList_ReturnsEmptyArray() throws Exception {
        // Arrange
        when(blogPostService.getAllPosts()).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(0)));

        verify(blogPostService).getAllPosts();
    }

    @Test
    void testGetPostBySlug_ValidSlug_Returns200() throws Exception {
        // Arrange
        when(blogPostService.getPostBySlug("test-post")).thenReturn(testPost);

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/test-post"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.slug").value("test-post"))
            .andExpect(jsonPath("$.title").value("Test Post"))
            .andExpect(jsonPath("$.content").value("Test content"))
            .andExpect(jsonPath("$.tags", hasSize(2)))
            .andExpect(jsonPath("$.tags[0]").value("java"))
            .andExpect(jsonPath("$.media", hasSize(1)))
            .andExpect(jsonPath("$.coverImage.url").value("https://example.com/cover.jpg"));

        verify(blogPostService).getPostBySlug("test-post");
    }

    @Test
    void testGetPostBySlug_InvalidSlug_Returns404() throws Exception {
        // Arrange
        when(blogPostService.getPostBySlug("invalid-slug"))
            .thenThrow(new ResourceNotFoundException("Post not found with slug: invalid-slug"));

        // Act & Assert
        mockMvc.perform(get("/api/v1/posts/invalid-slug"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.status").value(404))
            .andExpect(jsonPath("$.message").value("Post not found with slug: invalid-slug"));

        verify(blogPostService).getPostBySlug("invalid-slug");
    }

    @Test
    void testSearchPosts_ValidTerm_Returns200WithResults() throws Exception {
        // Arrange
        List<BlogPost> posts = Arrays.asList(testPost);
        when(blogPostService.searchPosts("test")).thenReturn(posts);

        // Act & Assert
        mockMvc.perform(get("/api/v1/search")
                .param("searchTerm", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.searchTerm").value("test"))
            .andExpect(jsonPath("$.results", hasSize(1)))
            .andExpect(jsonPath("$.results[0].title").value("Test Post"));

        verify(blogPostService).searchPosts("test");
    }

    @Test
    void testSearchPosts_NoMatches_ReturnsEmptyResults() throws Exception {
        // Arrange
        when(blogPostService.searchPosts("nonexistent")).thenReturn(Collections.emptyList());

        // Act & Assert
        mockMvc.perform(get("/api/v1/search")
                .param("searchTerm", "nonexistent"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.searchTerm").value("nonexistent"))
            .andExpect(jsonPath("$.results", hasSize(0)));

        verify(blogPostService).searchPosts("nonexistent");
    }
}
