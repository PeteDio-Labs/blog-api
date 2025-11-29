package com.petedillo.api.service;

import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.repository.BlogPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @InjectMocks
    private BlogPostService blogPostService;

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
        coverImage.setAltText("Cover image");
        testPost.getMedia().add(coverImage);
    }

    @Test
    void testGetAllPosts_ReturnsListOfPosts() {
        // Arrange
        List<BlogPost> posts = Arrays.asList(testPost);
        when(blogPostRepository.findAll()).thenReturn(posts);

        // Act
        List<BlogPost> result = blogPostService.getAllPosts();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Post", result.get(0).getTitle());
        verify(blogPostRepository).findAll();
    }

    @Test
    void testGetAllPosts_EmptyList_ReturnsEmptyList() {
        // Arrange
        when(blogPostRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<BlogPost> result = blogPostService.getAllPosts();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blogPostRepository).findAll();
    }

    @Test
    void testGetPostBySlug_ValidSlug_ReturnsPost() {
        // Arrange
        when(blogPostRepository.findBySlug("test-post"))
            .thenReturn(Optional.of(testPost));

        // Act
        BlogPost result = blogPostService.getPostBySlug("test-post");

        // Assert
        assertNotNull(result);
        assertEquals("Test Post", result.getTitle());
        assertEquals("test-post", result.getSlug());
        assertNotNull(result.getTags());
        assertEquals(2, result.getTags().size());
        assertNotNull(result.getMedia());
        assertEquals(1, result.getMedia().size());
        assertEquals(0, result.getMedia().get(0).getDisplayOrder());
        verify(blogPostRepository).findBySlug("test-post");
    }

    @Test
    void testGetPostBySlug_PostWithMultipleMedia_ReturnsSortedMedia() {
        // Arrange
        BlogMedia media2 = new BlogMedia();
        media2.setId(2L);
        media2.setBlogPost(testPost);
        media2.setMediaType(BlogMedia.MediaType.IMAGE);
        media2.setFilePath("images/test.jpg");
        media2.setDisplayOrder(1);
        testPost.getMedia().add(media2);
        
        when(blogPostRepository.findBySlug("test-post"))
            .thenReturn(Optional.of(testPost));

        // Act
        BlogPost result = blogPostService.getPostBySlug("test-post");

        // Assert
        assertNotNull(result.getMedia());
        assertEquals(2, result.getMedia().size());
        assertEquals(0, result.getMedia().get(0).getDisplayOrder());
        assertEquals(1, result.getMedia().get(1).getDisplayOrder());
        verify(blogPostRepository).findBySlug("test-post");
    }

    @Test
    void testGetPostBySlug_InvalidSlug_ThrowsResourceNotFoundException() {
        // Arrange
        when(blogPostRepository.findBySlug("invalid-slug"))
            .thenReturn(Optional.empty());

        // Act & Assert
        ResourceNotFoundException exception = assertThrows(
            ResourceNotFoundException.class,
            () -> blogPostService.getPostBySlug("invalid-slug")
        );

        assertEquals("Post not found with slug: invalid-slug", exception.getMessage());
        verify(blogPostRepository).findBySlug("invalid-slug");
    }

    @Test
    void testSearchPosts_ValidTerm_ReturnsMatchingPosts() {
        // Arrange
        List<BlogPost> posts = Arrays.asList(testPost);
        when(blogPostRepository.searchByTitleOrSlug("test"))
            .thenReturn(posts);

        // Act
        List<BlogPost> result = blogPostService.searchPosts("test");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals("Test Post", result.get(0).getTitle());
        verify(blogPostRepository).searchByTitleOrSlug("test");
    }

    @Test
    void testSearchPosts_NoMatches_ReturnsEmptyList() {
        // Arrange
        when(blogPostRepository.searchByTitleOrSlug("nonexistent"))
            .thenReturn(Collections.emptyList());

        // Act
        List<BlogPost> result = blogPostService.searchPosts("nonexistent");

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(blogPostRepository).searchByTitleOrSlug("nonexistent");
    }
}
