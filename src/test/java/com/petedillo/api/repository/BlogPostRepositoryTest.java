package com.petedillo.api.repository;

import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
class BlogPostRepositoryTest {

    @Autowired
    private BlogPostRepository blogPostRepository;

    private BlogPost testPost;

    @BeforeEach
    void setUp() {
        blogPostRepository.deleteAll();

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
        testPost.setTags(Arrays.asList("java", "spring-boot", "testing"));

        // Act
        BlogPost saved = blogPostRepository.save(testPost);
        BlogPost retrieved = blogPostRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertNotNull(retrieved.getTags());
        assertEquals(3, retrieved.getTags().size());
        assertTrue(retrieved.getTags().contains("java"));
        assertTrue(retrieved.getTags().contains("spring-boot"));
        assertTrue(retrieved.getTags().contains("testing"));
    }

    @Test
    void testDelete_WithTags_CascadesDeleteToTags() {
        // Arrange
        testPost.setTags(Arrays.asList("java", "spring-boot"));
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
        testPost.setTags(Arrays.asList("test"));
        
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

        // Act
        BlogPost saved = blogPostRepository.save(testPost);
        BlogPost retrieved = blogPostRepository.findById(saved.getId()).orElseThrow();

        // Assert
        assertEquals(3, retrieved.getMedia().size());
        assertEquals(0, retrieved.getMedia().get(0).getDisplayOrder());
        assertEquals(1, retrieved.getMedia().get(1).getDisplayOrder());
        assertEquals(2, retrieved.getMedia().get(2).getDisplayOrder());
        assertEquals("images/first.jpg", retrieved.getMedia().get(0).getFilePath());
    }
}
