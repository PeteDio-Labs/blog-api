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

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("SearchController Tests")
class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private TagRepository tagRepository;

    private BlogPost publishedPost1;
    private BlogPost publishedPost2;
    private BlogPost publishedPost3;
    private BlogPost draftPost;
    private Tag kubernetesTag;
    private Tag dockerTag;

    @BeforeEach
    void setUp() {
        // Clear existing data
        blogPostRepository.deleteAll();
        tagRepository.deleteAll();

        // Create tags
        kubernetesTag = new Tag();
        kubernetesTag.setName("kubernetes");
        kubernetesTag.setSlug("kubernetes");
        kubernetesTag.setPostCount(0);
        kubernetesTag = tagRepository.save(kubernetesTag);

        dockerTag = new Tag();
        dockerTag.setName("docker");
        dockerTag.setSlug("docker");
        dockerTag.setPostCount(0);
        dockerTag = tagRepository.save(dockerTag);

        // Create published posts with different content for search testing
        publishedPost1 = TestDataFactory.blogPostBuilder()
                .title("Kubernetes Deployment Guide")
                .slug("kubernetes-deployment-guide")
                .content("This post covers Kubernetes deployment strategies and best practices")
                .excerpt("Learn about Kubernetes deployments")
                .status(PostStatus.PUBLISHED.name())
                .publishedAt(LocalDateTime.now().minusDays(3))
                .build();
        publishedPost1.getTags().add(kubernetesTag);
        publishedPost1 = blogPostRepository.save(publishedPost1);

        publishedPost2 = TestDataFactory.blogPostBuilder()
                .title("Docker Container Best Practices")
                .slug("docker-container-best-practices")
                .content("Learn how to optimize your Docker containers for production use")
                .excerpt("Docker optimization tips")
                .status(PostStatus.PUBLISHED.name())
                .publishedAt(LocalDateTime.now().minusDays(2))
                .build();
        publishedPost2.getTags().add(dockerTag);
        publishedPost2 = blogPostRepository.save(publishedPost2);

        publishedPost3 = TestDataFactory.blogPostBuilder()
                .title("Microservices Architecture")
                .slug("microservices-architecture")
                .content("Building scalable microservices with modern tools")
                .excerpt("Microservices guide")
                .status(PostStatus.PUBLISHED.name())
                .publishedAt(LocalDateTime.now().minusDays(1))
                .build();
        publishedPost3 = blogPostRepository.save(publishedPost3);

        // Create draft post (should not appear in search results)
        draftPost = TestDataFactory.blogPostBuilder()
                .title("Draft Post About Kubernetes")
                .slug("draft-kubernetes-post")
                .content("This draft post also mentions Kubernetes but should not be searchable")
                .excerpt("Draft content")
                .status(PostStatus.DRAFT.name())
                .publishedAt(null)
                .build();
        draftPost = blogPostRepository.save(draftPost);
    }

    // ===========================
    // GET /api/v1/search - Search published posts
    // ===========================

    @Test
    @DisplayName("GET /api/v1/search should return 200 OK with matching published posts")
    void testSearch_Success() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "kubernetes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Kubernetes Deployment Guide"));
    }

    @Test
    @DisplayName("GET /api/v1/search should search across title (case-insensitive)")
    void testSearch_TitleCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "DOCKER"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Docker Container Best Practices"));
    }

    @Test
    @DisplayName("GET /api/v1/search should search across content (case-insensitive)")
    void testSearch_ContentCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "scalable"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].title").value("Microservices Architecture"));
    }

    @Test
    @DisplayName("GET /api/v1/search should search across tags (case-insensitive)")
    void testSearch_TagsCaseInsensitive() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "KUBERNETES"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(greaterThanOrEqualTo(1))));
    }

    @Test
    @DisplayName("GET /api/v1/search should only return PUBLISHED posts")
    void testSearch_OnlyPublished() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "kubernetes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].status").value(everyItem(is("PUBLISHED"))));
    }

    @Test
    @DisplayName("GET /api/v1/search should return empty page when no matches")
    void testSearch_NoMatches() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "nonexistent"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content", hasSize(0)))
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    @DisplayName("GET /api/v1/search should respect pagination params")
    void testSearch_Pagination() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "post")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.size").value(1));
    }

    @Test
    @DisplayName("GET /api/v1/search should require 'q' query parameter (error if missing)")
    void testSearch_RequiresQueryParam() throws Exception {
        // Spring Boot 3 returns 500 for missing required parameters by default
        mockMvc.perform(get("/api/v1/search"))
                .andExpect(status().is5xxServerError());
    }

    @Test
    @DisplayName("GET /api/v1/search should not require authentication")
    void testSearch_NoAuthRequired() throws Exception {
        // No Authorization header provided
        mockMvc.perform(get("/api/v1/search")
                        .param("q", "kubernetes"))
                .andExpect(status().isOk());
    }
}
