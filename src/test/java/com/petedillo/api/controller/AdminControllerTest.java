package com.petedillo.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.petedillo.api.dto.BlogPostRequest;
import com.petedillo.api.model.AdminUser;
import com.petedillo.api.model.AuthProvider;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.PostStatus;
import com.petedillo.api.repository.AdminUserRepository;
import com.petedillo.api.repository.BlogPostRepository;
import com.petedillo.api.security.JwtTokenProvider;
import com.petedillo.api.test.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Admin Controllers Tests")
class AdminControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AdminUserRepository adminUserRepository;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private AdminUser testUser;
    private String validAccessToken;

    @BeforeEach
    void setUp() {
        // Create test user
        testUser = TestDataFactory.adminUserBuilder()
                .username("admin")
                .email("admin@test.com")
                .authProvider(AuthProvider.LOCAL)
                .passwordHash(passwordEncoder.encode("admin123"))
                .isEnabled(true)
                .build();
        adminUserRepository.save(testUser);

        // Generate valid access token
        validAccessToken = tokenProvider.generateAccessToken(testUser);
    }

    @Test
    @DisplayName("should create blog post with valid JWT token")
    void testCreateBlogPostWithAuth() throws Exception {
        // Arrange
        BlogPostRequest request = new BlogPostRequest();
        request.setTitle("Test Post");
        request.setContent("Test content");
        request.setStatus(PostStatus.DRAFT);

        // Act & Assert
        mockMvc.perform(post("/api/v1/admin/posts")
                .header("Authorization", "Bearer " + validAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    @DisplayName("should reject post creation without authentication token")
    void testCreateBlogPostWithoutAuth() throws Exception {
        // Arrange
        BlogPostRequest request = new BlogPostRequest();
        request.setTitle("Test Post");
        request.setContent("Test content");

        // Act & Assert - expect 403 Forbidden when not authenticated
        mockMvc.perform(post("/api/v1/admin/posts")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("should retrieve blog post by ID")
    void testGetBlogPostById() throws Exception {
        // Arrange
        BlogPost post = TestDataFactory.blogPostBuilder()
                .title("Test Post")
                .content("Test content")
                .status(PostStatus.PUBLISHED)
                .build();
        BlogPost savedPost = blogPostRepository.save(post);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/posts/" + savedPost.getId())
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(savedPost.getId()))
                .andExpect(jsonPath("$.title").value("Test Post"));
    }

    @Test
    @DisplayName("should return 404 for non-existent post")
    void testGetNonExistentPost() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/posts/99999")
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should list all posts with pagination")
    void testListBlogPosts() throws Exception {
        // Arrange
        for (int i = 0; i < 3; i++) {
            BlogPost post = TestDataFactory.blogPostBuilder()
                    .title("Post " + i)
                    .content("Content " + i)
                    .status(PostStatus.PUBLISHED)
                    .build();
            blogPostRepository.save(post);
        }

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/posts?page=0&size=10")
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content.length()").value(3));
    }

    @Test
    @DisplayName("should update blog post with valid data")
    void testUpdateBlogPost() throws Exception {
        // Arrange
        BlogPost post = TestDataFactory.blogPostBuilder()
                .title("Original Title")
                .content("Original content")
                .status(PostStatus.DRAFT)
                .build();
        BlogPost savedPost = blogPostRepository.save(post);

        BlogPostRequest updateRequest = new BlogPostRequest();
        updateRequest.setTitle("Updated Title");
        updateRequest.setContent("Updated content");
        updateRequest.setStatus(PostStatus.PUBLISHED);

        // Act & Assert
        mockMvc.perform(put("/api/v1/admin/posts/" + savedPost.getId())
                .header("Authorization", "Bearer " + validAccessToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Title"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    @DisplayName("should delete blog post")
    void testDeleteBlogPost() throws Exception {
        // Arrange
        BlogPost post = TestDataFactory.blogPostBuilder()
                .title("To Delete")
                .content("This will be deleted")
                .status(PostStatus.DRAFT)
                .build();
        BlogPost savedPost = blogPostRepository.save(post);

        // Act & Assert
        mockMvc.perform(delete("/api/v1/admin/posts/" + savedPost.getId())
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isNoContent());

        // Verify post is deleted
        mockMvc.perform(get("/api/v1/admin/posts/" + savedPost.getId())
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("should filter posts by status")
    void testFilterPostsByStatus() throws Exception {
        // Arrange
        BlogPost draftPost = TestDataFactory.blogPostBuilder()
                .title("Draft Post")
                .status(PostStatus.DRAFT)
                .build();
        BlogPost publishedPost = TestDataFactory.blogPostBuilder()
                .title("Published Post")
                .status(PostStatus.PUBLISHED)
                .build();
        blogPostRepository.save(draftPost);
        blogPostRepository.save(publishedPost);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/posts?status=PUBLISHED")
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    @DisplayName("should search posts by title")
    void testSearchPostsByTitle() throws Exception {
        // Arrange
        BlogPost post = TestDataFactory.blogPostBuilder()
                .title("Unique Post Title")
                .content("Some content")
                .status(PostStatus.PUBLISHED)
                .build();
        blogPostRepository.save(post);

        // Act & Assert
        mockMvc.perform(get("/api/v1/admin/posts?search=Unique")
                .header("Authorization", "Bearer " + validAccessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].title").value("Unique Post Title"));
    }
}
