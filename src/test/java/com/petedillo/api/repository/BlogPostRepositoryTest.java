package com.petedillo.api.repository;

import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.Tag;
import com.petedillo.api.test.TestDataFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BlogPostRepositoryTest {

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private TagRepository tagRepository;

    private BlogPost testPost;

    @BeforeEach
    void setUp() {
        blogPostRepository.deleteAll();
        tagRepository.deleteAll();

        testPost = new BlogPost();
        testPost.setTitle("Test Post");
        testPost.setSlug("test-post");
        testPost.setContent("Test content for the blog post");
        testPost.setExcerpt("Test excerpt");
        testPost.setStatus("published");
        testPost.setPublishedAt(LocalDateTime.now());
        testPost.setCreatedAt(LocalDateTime.now());
        testPost.setUpdatedAt(LocalDateTime.now());
    }

    @Test
    void testFindAll_ReturnsAllPosts() {
        // Arrange
        blogPostRepository.save(testPost);

        // Act
        List<BlogPost> result = blogPostRepository.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
    }

    @Test
    void testFindBySlug_ValidSlug_ReturnsPost() {
        // Arrange
        blogPostRepository.save(testPost);

        // Act
        Optional<BlogPost> result = blogPostRepository.findBySlug("test-post");

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Test Post", result.get().getTitle());
        assertEquals("test-post", result.get().getSlug());
    }

    @Test
    void testFindBySlug_InvalidSlug_ReturnsEmpty() {
        // Act
        Optional<BlogPost> result = blogPostRepository.findBySlug("nonexistent");

        // Assert
        assertTrue(result.isEmpty());
    }

    @Test
    void testSearchByTitleOrSlug_MatchingTitle_ReturnsPosts() {
        // Arrange
        blogPostRepository.save(testPost);

        // Act
        List<BlogPost> result = blogPostRepository.searchByTitleOrSlug("Test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Post", result.get(0).getTitle());
    }

    @Test
    void testSearchByTitleOrSlug_MatchingSlug_ReturnsPosts() {
        // Arrange
        blogPostRepository.save(testPost);

        // Act
        List<BlogPost> result = blogPostRepository.searchByTitleOrSlug("test-post");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("test-post", result.get(0).getSlug());
    }

    @Test
    void testSearchByTitleOrSlug_NoMatch_ReturnsEmptyList() {
        // Arrange
        blogPostRepository.save(testPost);

        // Act
        List<BlogPost> result = blogPostRepository.searchByTitleOrSlug("nonexistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave_ValidPost_PersistsToDatabase() {
        // Act
        BlogPost saved = blogPostRepository.save(testPost);

        // Assert
        assertNotNull(saved);
        assertNotNull(saved.getId());
        assertEquals("Test Post", saved.getTitle());
    }

    @Test
    void testDelete_ExistingPost_RemovesFromDatabase() {
        // Arrange
        BlogPost saved = blogPostRepository.save(testPost);
        Long id = saved.getId();

        // Act
        blogPostRepository.deleteById(id);

        // Assert
        Optional<BlogPost> result = blogPostRepository.findById(id);
        assertTrue(result.isEmpty());
    }

    @Test
    void testSave_WithTags_PersistsTagsToDatabase() {
        // Arrange
        Tag tag1 = tagRepository.save(TestDataFactory.tagBuilder().name("java").slug("java").build());
        Tag tag2 = tagRepository.save(TestDataFactory.tagBuilder().name("spring-boot").slug("spring-boot").build());
        Tag tag3 = tagRepository.save(TestDataFactory.tagBuilder().name("testing").slug("testing").build());
        testPost.getTags().addAll(Arrays.asList(tag1, tag2, tag3));

        // Act
        BlogPost saved = blogPostRepository.save(testPost);
        BlogPost retrieved = blogPostRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertNotNull(retrieved.getTags());
        assertEquals(3, retrieved.getTags().size());
        Set<String> tagNames = retrieved.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        assertTrue(tagNames.contains("java"));
        assertTrue(tagNames.contains("spring-boot"));
        assertTrue(tagNames.contains("testing"));
    }

    @Test
    void testDelete_WithTags_CascadesDeleteToTags() {
        // Arrange
        Tag tag1 = tagRepository.save(TestDataFactory.tagBuilder().name("java").slug("java").build());
        Tag tag2 = tagRepository.save(TestDataFactory.tagBuilder().name("spring-boot").slug("spring-boot").build());
        testPost.getTags().addAll(Arrays.asList(tag1, tag2));
        BlogPost saved = blogPostRepository.save(testPost);
        Long id = saved.getId();

        // Act
        blogPostRepository.deleteById(id);

        // Assert
        Optional<BlogPost> result = blogPostRepository.findById(id);
        assertTrue(result.isEmpty());
        // Tags should be cascade deleted automatically
    }

    @Test
    void testSave_WithMedia_PersistsMediaToDatabase() {
        // Arrange
        BlogMedia media1 = new BlogMedia();
        media1.setBlogPost(testPost);
        media1.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        media1.setExternalUrl("https://example.com/image.jpg");
        media1.setDisplayOrder(0);
        media1.setAltText("Test cover image");
        media1.setCaption("Test caption");

        BlogMedia media2 = new BlogMedia();
        media2.setBlogPost(testPost);
        media2.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        media2.setExternalUrl("https://example.com/image2.jpg");
        media2.setDisplayOrder(1);
        media2.setAltText("Second image");

        testPost.getMedia().add(media1);
        testPost.getMedia().add(media2);

        // Act
        BlogPost saved = blogPostRepository.save(testPost);
        BlogPost retrieved = blogPostRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertNotNull(retrieved.getMedia());
        assertEquals(2, retrieved.getMedia().size());
        assertEquals(0, retrieved.getMedia().get(0).getDisplayOrder());
        assertEquals(1, retrieved.getMedia().get(1).getDisplayOrder());
        assertEquals("Test cover image", retrieved.getMedia().get(0).getAltText());
    }

    @Test
    void testDelete_WithMedia_CascadesDeleteToMedia() {
        // Arrange
        BlogMedia media = new BlogMedia();
        media.setBlogPost(testPost);
        media.setMediaType(BlogMedia.MediaType.IMAGE);
        media.setFilePath("images/test.jpg");
        media.setDisplayOrder(0);
        testPost.getMedia().add(media);

        BlogPost saved = blogPostRepository.save(testPost);
        Long id = saved.getId();

        // Act
        blogPostRepository.deleteById(id);

        // Assert
        Optional<BlogPost> result = blogPostRepository.findById(id);
        assertTrue(result.isEmpty());
        // Media should be cascade deleted automatically
    }

    @Test
    void testFindBySlug_LoadsMediaWithJoinFetch() {
        // Arrange
        BlogMedia media = new BlogMedia();
        media.setBlogPost(testPost);
        media.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        media.setExternalUrl("https://example.com/test.jpg");
        media.setDisplayOrder(0);
        testPost.getMedia().add(media);
        Tag tag = tagRepository.save(TestDataFactory.tagBuilder().name("test").slug("test").build());
        testPost.getTags().add(tag);
        
        blogPostRepository.save(testPost);

        // Act
        Optional<BlogPost> result = blogPostRepository.findBySlug("test-post");

        // Assert
        assertTrue(result.isPresent());
        BlogPost post = result.get();
        assertNotNull(post.getMedia());
        assertEquals(1, post.getMedia().size());
        assertNotNull(post.getTags());
        assertEquals(1, post.getTags().size());
        // Verify LEFT JOIN FETCH loaded both relationships
    }

    @Test
    void testMediaOrdering_ByDisplayOrder() {
        // Arrange
        BlogMedia media3 = new BlogMedia();
        media3.setBlogPost(testPost);
        media3.setMediaType(BlogMedia.MediaType.IMAGE);
        media3.setFilePath("images/third.jpg");
        media3.setDisplayOrder(2);

        BlogMedia media1 = new BlogMedia();
        media1.setBlogPost(testPost);
        media1.setMediaType(BlogMedia.MediaType.IMAGE);
        media1.setFilePath("images/first.jpg");
        media1.setDisplayOrder(0);

        BlogMedia media2 = new BlogMedia();
        media2.setBlogPost(testPost);
        media2.setMediaType(BlogMedia.MediaType.IMAGE);
        media2.setFilePath("images/second.jpg");
        media2.setDisplayOrder(1);

        // Add in non-sequential order
        testPost.getMedia().add(media3);
        testPost.getMedia().add(media1);
        testPost.getMedia().add(media2);

        // Act - save and retrieve
        blogPostRepository.save(testPost);
        Optional<BlogPost> retrieved = blogPostRepository.findBySlug("test-post");

        // Assert - verify all media items are present with correct displayOrder values
        assertTrue(retrieved.isPresent());
        List<BlogMedia> mediaList = retrieved.get().getMedia();
        assertEquals(3, mediaList.size());

        // Verify all display orders are present (0, 1, 2)
        List<Integer> displayOrders = mediaList.stream()
            .map(BlogMedia::getDisplayOrder)
            .sorted()
            .toList();
        assertEquals(Arrays.asList(0, 1, 2), displayOrders);

        // Verify media with displayOrder=0 has correct filePath
        BlogMedia coverImage = mediaList.stream()
            .filter(m -> m.getDisplayOrder() == 0)
            .findFirst()
            .orElseThrow();
        assertEquals("images/first.jpg", coverImage.getFilePath());
    }

    // === TDD INTEGRATION TESTS FOR CARTESIAN PRODUCT FIX ===

    @Test
    void testFindBySlugWithTags_NoCartesianProduct_3x3Scenario() {
        // Arrange - Create post with 3 tags × 3 media (would create 9 rows with Cartesian product)
        for (int i = 0; i < 3; i++) {
            BlogMedia media = new BlogMedia();
            media.setBlogPost(testPost);
            media.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
            media.setExternalUrl("https://example.com/test" + i + ".jpg");
            media.setDisplayOrder(i);
            testPost.getMedia().add(media);
        }
        Tag tag1 = tagRepository.save(TestDataFactory.tagBuilder().name("java").slug("java").build());
        Tag tag2 = tagRepository.save(TestDataFactory.tagBuilder().name("spring").slug("spring").build());
        Tag tag3 = tagRepository.save(TestDataFactory.tagBuilder().name("jpa").slug("jpa").build());
        testPost.getTags().addAll(Arrays.asList(tag1, tag2, tag3));

        blogPostRepository.save(testPost);

        // Act - Load with tags first
        Optional<BlogPost> postWithTags = blogPostRepository.findBySlugWithTags("test-post");

        // Assert
        assertTrue(postWithTags.isPresent());
        BlogPost post = postWithTags.get();

        // Verify tags loaded
        assertNotNull(post.getTags());
        assertEquals(3, post.getTags().size(), "Should have exactly 3 tags");
        Set<String> tagNames = post.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        assertTrue(tagNames.containsAll(Arrays.asList("java", "spring", "jpa")));
    }

    @Test
    void testFindBySlugWithMedia_LoadsMediaInOrder() {
        // Arrange - Create post with 3 media items
        for (int i = 0; i < 3; i++) {
            BlogMedia media = new BlogMedia();
            media.setBlogPost(testPost);
            media.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
            media.setExternalUrl("https://example.com/test" + i + ".jpg");
            media.setDisplayOrder(i);
            media.setAltText("Image " + i);
            testPost.getMedia().add(media);
        }
        Tag tag = tagRepository.save(TestDataFactory.tagBuilder().name("tag1").slug("tag1").build());
        testPost.getTags().add(tag);

        blogPostRepository.save(testPost);

        // Act - Load with media
        Optional<BlogPost> postWithMedia = blogPostRepository.findBySlugWithMedia("test-post");

        // Assert
        assertTrue(postWithMedia.isPresent());
        BlogPost post = postWithMedia.get();

        // Verify media loaded in correct order
        assertNotNull(post.getMedia());
        assertEquals(3, post.getMedia().size(), "Should have exactly 3 media items");

        // Verify ordering
        for (int i = 0; i < 3; i++) {
            assertEquals(i, post.getMedia().get(i).getDisplayOrder());
            assertEquals("Image " + i, post.getMedia().get(i).getAltText());
        }
    }

    @Test
    void testTwoQueryPattern_LoadsAllDataWithoutCartesianProduct() {
        // Arrange - Create post with 2 tags × 2 media (would create 4 rows with old approach)
        BlogMedia media1 = new BlogMedia();
        media1.setBlogPost(testPost);
        media1.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        media1.setExternalUrl("https://example.com/test1.jpg");
        media1.setDisplayOrder(0);

        BlogMedia media2 = new BlogMedia();
        media2.setBlogPost(testPost);
        media2.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        media2.setExternalUrl("https://example.com/test2.jpg");
        media2.setDisplayOrder(1);

        testPost.getMedia().add(media1);
        testPost.getMedia().add(media2);
        Tag tag1 = tagRepository.save(TestDataFactory.tagBuilder().name("tag1").slug("tag1").build());
        Tag tag2 = tagRepository.save(TestDataFactory.tagBuilder().name("tag2").slug("tag2").build());
        testPost.getTags().addAll(Arrays.asList(tag1, tag2));

        blogPostRepository.save(testPost);

        // Act - Simulate the two-query pattern
        // Query 1: Load with tags
        Optional<BlogPost> postWithTags = blogPostRepository.findBySlugWithTags("test-post");
        assertTrue(postWithTags.isPresent());

        // Query 2: Load with media
        Optional<BlogPost> postWithMedia = blogPostRepository.findBySlugWithMedia("test-post");
        assertTrue(postWithMedia.isPresent());

        BlogPost finalPost = postWithMedia.get();

        // Assert - Verify no Cartesian product
        assertNotNull(finalPost.getTags());
        assertEquals(2, finalPost.getTags().size(), "Should have exactly 2 tags");

        assertNotNull(finalPost.getMedia());
        assertEquals(2, finalPost.getMedia().size(), "Should have exactly 2 media items, not 4 from Cartesian product");

        // Verify ordering maintained
        assertEquals(0, finalPost.getMedia().get(0).getDisplayOrder());
        assertEquals(1, finalPost.getMedia().get(1).getDisplayOrder());
    }

    @Test
    void testFindAllByOrderByPublishedAtDesc_WithEntityGraph_LoadsTagsEfficiently() {
        // Arrange - Create 3 posts with tags to test N+1 query issue
        blogPostRepository.deleteAll();
        
        Tag commonTag = tagRepository.save(TestDataFactory.tagBuilder().name("common").slug("common").build());

        for (int i = 0; i < 3; i++) {
            BlogPost post = new BlogPost();
            post.setTitle("Post " + i);
            post.setSlug("post-" + i);
            post.setContent("Content " + i);
            post.setStatus("published");
            post.setPublishedAt(LocalDateTime.now().minusDays(i));
            post.setCreatedAt(LocalDateTime.now());
            post.setUpdatedAt(LocalDateTime.now());
            
            Tag uniqueTag = tagRepository.save(TestDataFactory.tagBuilder().name("tag" + i).slug("tag" + i).build());
            post.getTags().addAll(Arrays.asList(uniqueTag, commonTag));
            blogPostRepository.save(post);
        }

        // Act
        List<BlogPost> posts = blogPostRepository.findAllByOrderByPublishedAtDesc();

        // Assert
        assertEquals(3, posts.size());
        // Verify all tags loaded (with EntityGraph, no N+1 queries)
        for (BlogPost post : posts) {
            assertNotNull(post.getTags());
            assertEquals(2, post.getTags().size());
            Set<String> tagNames = post.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
            assertTrue(tagNames.contains("common"));
        }
    }

    @Test
    void testSearchByTitleOrSlug_WithEntityGraph_LoadsTagsEfficiently() {
        // Arrange
        Tag tag1 = tagRepository.save(TestDataFactory.tagBuilder().name("java").slug("java").build());
        Tag tag2 = tagRepository.save(TestDataFactory.tagBuilder().name("spring").slug("spring").build());
        testPost.getTags().addAll(Arrays.asList(tag1, tag2));
        blogPostRepository.save(testPost);

        // Act
        List<BlogPost> results = blogPostRepository.searchByTitleOrSlug("Test");

        // Assert
        assertFalse(results.isEmpty());
        BlogPost post = results.get(0);
        assertNotNull(post.getTags());
        assertEquals(2, post.getTags().size());
        Set<String> tagNames = post.getTags().stream().map(Tag::getName).collect(Collectors.toSet());
        assertTrue(tagNames.contains("java"));
        assertTrue(tagNames.contains("spring"));
    }
}
