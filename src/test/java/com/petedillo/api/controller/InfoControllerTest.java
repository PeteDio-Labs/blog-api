package com.petedillo.api.controller;

import com.petedillo.api.model.Tag;
import com.petedillo.api.repository.BlogPostRepository;
import com.petedillo.api.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static com.petedillo.api.test.TestDataFactory.*;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Integration tests for InfoController.
 * Tests the GET /api/v1/info endpoint which provides API metadata.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class InfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        blogPostRepository.deleteAll();
        tagRepository.deleteAll();
    }

    @Test
    @DisplayName("GET /api/v1/info should return 200 OK with API metadata")
    void testGetApiInfo_Success() throws Exception {
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiVersion").exists())
                .andExpect(jsonPath("$.environment").exists())
                .andExpect(jsonPath("$.totalPublishedPosts").exists())
                .andExpect(jsonPath("$.availableTags").exists());
    }

    @Test
    @DisplayName("GET /api/v1/info should include apiVersion from properties")
    void testGetApiInfo_IncludesApiVersion() throws Exception {
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiVersion").value(is(notNullValue())))
                .andExpect(jsonPath("$.apiVersion").isString());
    }

    @Test
    @DisplayName("GET /api/v1/info should include environment from properties")
    void testGetApiInfo_IncludesEnvironment() throws Exception {
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.environment").value(is(notNullValue())))
                .andExpect(jsonPath("$.environment").isString());
    }

    @Test
    @DisplayName("GET /api/v1/info should include totalPublishedPosts count")
    void testGetApiInfo_IncludesTotalPublishedPosts() throws Exception {
        // Create 3 published posts and 1 draft
        blogPostRepository.save(blogPostBuilder()
                .title("Post 1")
                .slug("post-1")
                .status("PUBLISHED")
                .publishedAt(LocalDateTime.now())
                .build());
        blogPostRepository.save(blogPostBuilder()
                .title("Post 2")
                .slug("post-2")
                .status("PUBLISHED")
                .publishedAt(LocalDateTime.now())
                .build());
        blogPostRepository.save(blogPostBuilder()
                .title("Post 3")
                .slug("post-3")
                .status("PUBLISHED")
                .publishedAt(LocalDateTime.now())
                .build());
        blogPostRepository.save(blogPostBuilder()
                .title("Draft Post")
                .slug("draft-post")
                .status("DRAFT")
                .build());

        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPublishedPosts").value(3));
    }

    @Test
    @DisplayName("GET /api/v1/info should return 0 when no published posts exist")
    void testGetApiInfo_NoPublishedPosts() throws Exception {
        // Create only draft posts
        blogPostRepository.save(blogPostBuilder()
                .title("Draft Post")
                .slug("draft-post")
                .status("DRAFT")
                .build());

        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalPublishedPosts").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/info should include recentPostDate")
    void testGetApiInfo_IncludesRecentPostDate() throws Exception {
        // Create published post with specific publishedAt date
        blogPostRepository.save(blogPostBuilder()
                .title("Recent Post")
                .slug("recent-post")
                .status("PUBLISHED")
                .publishedAt(LocalDateTime.of(2025, 12, 9, 10, 30))
                .build());

        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentPostDate").value("2025-12-09T10:30:00"));
    }

    @Test
    @DisplayName("GET /api/v1/info should return null recentPostDate when no published posts")
    void testGetApiInfo_NullRecentPostDateWhenNoPublishedPosts() throws Exception {
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.recentPostDate").value(is(nullValue())));
    }

    @Test
    @DisplayName("GET /api/v1/info should include list of available tags")
    void testGetApiInfo_IncludesAvailableTags() throws Exception {
        // Create tags
        Tag tag1 = new Tag();
        tag1.setName("kubernetes");
        tag1.setSlug("kubernetes");
        tag1.setPostCount(5);
        tagRepository.save(tag1);

        Tag tag2 = new Tag();
        tag2.setName("docker");
        tag2.setSlug("docker");
        tag2.setPostCount(3);
        tagRepository.save(tag2);

        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTags").isArray())
                .andExpect(jsonPath("$.availableTags", hasSize(2)))
                .andExpect(jsonPath("$.availableTags", containsInAnyOrder("kubernetes", "docker")));
    }

    @Test
    @DisplayName("GET /api/v1/info should return empty array when no tags exist")
    void testGetApiInfo_EmptyTagsArray() throws Exception {
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.availableTags").isArray())
                .andExpect(jsonPath("$.availableTags", hasSize(0)));
    }

    @Test
    @DisplayName("GET /api/v1/info should not require authentication")
    void testGetApiInfo_NoAuthenticationRequired() throws Exception {
        // No Authorization header - should still work
        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/v1/info should include all metadata fields in response")
    void testGetApiInfo_AllFieldsPresent() throws Exception {
        // Create some test data
        blogPostRepository.save(blogPostBuilder()
                .title("Test Post")
                .slug("test-post")
                .status("PUBLISHED")
                .publishedAt(LocalDateTime.now())
                .build());

        Tag tag = new Tag();
        tag.setName("java");
        tag.setSlug("java");
        tag.setPostCount(1);
        tagRepository.save(tag);

        mockMvc.perform(get("/api/v1/info"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.apiVersion").exists())
                .andExpect(jsonPath("$.environment").exists())
                .andExpect(jsonPath("$.totalPublishedPosts").value(1))
                .andExpect(jsonPath("$.recentPostDate").exists())
                .andExpect(jsonPath("$.availableTags").isArray())
                .andExpect(jsonPath("$.availableTags", hasSize(1)))
                .andExpect(jsonPath("$.availableTags[0]").value("java"));
    }
}
