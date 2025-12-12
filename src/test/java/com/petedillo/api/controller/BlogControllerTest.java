package com.petedillo.api.controller;

import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.PostStatus;
import com.petedillo.api.model.Tag;
import com.petedillo.api.repository.BlogPostRepository;
import com.petedillo.api.repository.TagRepository;
import com.petedillo.api.test.TestDataFactory;
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
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("BlogController Tests")
class BlogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private TagRepository tagRepository;

    private BlogPost publishedPost1;
    private BlogPost publishedPost2;
    private BlogPost draftPost;
    private Tag tag1;
    private Tag tag2;

    @BeforeEach
    void setUp() {
        // Clear existing data
        blogPostRepository.deleteAll();
        tagRepository.deleteAll();

        // Create tags
        tag1 = new Tag();
        tag1.setName("kubernetes");
        tag1.setSlug("kubernetes");
        tag1.setPostCount(0);
        tag1 = tagRepository.save(tag1);

        tag2 = new Tag();
        tag2.setName("docker");
        tag2.setSlug("docker");
        tag2.setPostCount(0);
        tag2 = tagRepository.save(tag2);

        // Create published posts
        publishedPost1 = TestDataFactory.blogPostBuilder()
                .title("First Published Post")
                .slug("first-published-post")
                .content("This is the content of the first published post")
                .excerpt("First post excerpt")
                .status(PostStatus.PUBLISHED.name())
                .publishedAt(LocalDateTime.now().minusDays(2))
                .build();

        // Add tags directly via getTags()
        publishedPost1.getTags().add(tag1);
        publishedPost1.getTags().add(tag2);
        publishedPost1 = blogPostRepository.save(publishedPost1);

        publishedPost2 = TestDataFactory.blogPostBuilder()
                .title("Second Published Post")
                .slug("second-published-post")
                .content("This is the content of the second published post")
                .excerpt("Second post excerpt")
                .status(PostStatus.PUBLISHED.name())
                .publishedAt(LocalDateTime.now().minusDays(1))
                .build();

        // Add tags directly via getTags()
        publishedPost2.getTags().add(tag1);
        publishedPost2 = blogPostRepository.save(publishedPost2);

        // Create draft post (should not be visible publicly)
        draftPost = TestDataFactory.blogPostBuilder()
                .title("Draft Post")
                .slug("draft-post")
                .content("This is a draft post")
                .excerpt("Draft excerpt")
                .status(PostStatus.DRAFT.name())
                .publishedAt(null)
                .build();
        draftPost = blogPostRepository.save(draftPost);
    }

    // ===========================
    // GET /api/v1/posts - List published posts
    // ===========================

    @Test
    @DisplayName("GET /api/v1/posts should return 200 OK with paginated published posts")
    void testGetPublishedPosts_Success() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.content[0].title").value("Second Published Post"))
                .andExpect(jsonPath("$.content[1].title").value("First Published Post"));
    }

    @Test
    @DisplayName("GET /api/v1/posts should only return PUBLISHED status posts")
    void testGetPublishedPosts_OnlyPublished() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[*].status").value(everyItem(is("PUBLISHED"))));
    }

    @Test
    @DisplayName("GET /api/v1/posts should include tags in response")
    void testGetPublishedPosts_IncludesTags() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tags").isArray())
                .andExpect(jsonPath("$.content[0].tags", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/posts should respect pagination params")
    void testGetPublishedPosts_Pagination() throws Exception {
        mockMvc.perform(get("/api/v1/posts")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    @DisplayName("GET /api/v1/posts should sort by publishedAt desc by default")
    void testGetPublishedPosts_SortedByPublishedAtDesc() throws Exception {
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].title").value("Second Published Post"))
                .andExpect(jsonPath("$.content[1].title").value("First Published Post"));
    }

    @Test
    @DisplayName("GET /api/v1/posts should return empty page when no published posts exist")
    void testGetPublishedPosts_EmptyWhenNoPosts() throws Exception {
        // Delete all published posts
        blogPostRepository.delete(publishedPost1);
        blogPostRepository.delete(publishedPost2);

        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/posts should not require authentication")
    void testGetPublishedPosts_NoAuthRequired() throws Exception {
        // No Authorization header provided
        mockMvc.perform(get("/api/v1/posts"))
                .andExpect(status().isOk());
    }

    // ===========================
    // GET /api/v1/posts/{slug} - Get post by slug
    // ===========================

    @Test
    @DisplayName("GET /api/v1/posts/{slug} should return 200 OK with full post details when slug exists and post is published")
    void testGetPostBySlug_Success() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{slug}", "first-published-post"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(publishedPost1.getId()))
                .andExpect(jsonPath("$.title").value("First Published Post"))
                .andExpect(jsonPath("$.slug").value("first-published-post"))
                .andExpect(jsonPath("$.content").value("This is the content of the first published post"))
                .andExpect(jsonPath("$.excerpt").value("First post excerpt"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"))
                .andExpect(jsonPath("$.tags").isArray())
                .andExpect(jsonPath("$.tags", hasSize(2)));
    }

    @Test
    @DisplayName("GET /api/v1/posts/{slug} should return 404 NOT FOUND when slug doesn't exist")
    void testGetPostBySlug_NotFound() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{slug}", "non-existent-slug"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/posts/{slug} should return 404 NOT FOUND when post exists but is DRAFT")
    void testGetPostBySlug_DraftNotFound() throws Exception {
        mockMvc.perform(get("/api/v1/posts/{slug}", "draft-post"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/posts/{slug} should increment view count")
    void testGetPostBySlug_IncrementsViewCount() throws Exception {
        // Get initial view count
        int initialViewCount = publishedPost1.getViewCount();

        // Request the post
        mockMvc.perform(get("/api/v1/posts/{slug}", "first-published-post"))
                .andExpect(status().isOk());

        // Verify view count was incremented
        BlogPost updatedPost = blogPostRepository.findById(publishedPost1.getId()).orElseThrow();
        assert updatedPost.getViewCount() == initialViewCount + 1 : "View count should be incremented";
    }

    @Test
    @DisplayName("GET /api/v1/posts/{slug} should not require authentication")
    void testGetPostBySlug_NoAuthRequired() throws Exception {
        // No Authorization header provided
        mockMvc.perform(get("/api/v1/posts/{slug}", "first-published-post"))
                .andExpect(status().isOk());
    }
}
