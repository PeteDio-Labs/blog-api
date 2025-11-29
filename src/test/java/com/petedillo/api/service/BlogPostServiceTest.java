package com.petedillo.api.service;

import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.model.BlogTag;
import com.petedillo.api.repository.BlogMediaRepository;
import com.petedillo.api.repository.BlogPostRepository;
import com.petedillo.api.repository.BlogTagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BlogPostServiceTest {

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private BlogTagRepository blogTagRepository;

    @Mock
    private BlogMediaRepository blogMediaRepository;

    @Mock
    private FileStorageService fileStorageService;

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

    // === NEW ADMIN CRUD TESTS ===

    @Test
    void testCreatePost_WithTags_CreatesPostSuccessfully() {
        // Arrange
        Set<String> tagNames = new HashSet<>(Arrays.asList("Java", "Spring Boot"));
        BlogPost savedPost = new BlogPost();
        savedPost.setId(1L);
        savedPost.setTitle("New Post");

        BlogTag javaTag = new BlogTag();
        javaTag.setId(1L);
        javaTag.setTagName("java");
        javaTag.setBlogPost(savedPost);

        BlogTag springTag = new BlogTag();
        springTag.setId(2L);
        springTag.setTagName("spring boot");
        springTag.setBlogPost(savedPost);

        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(savedPost);
        when(blogTagRepository.findByTagNameIgnoreCase("java")).thenReturn(Optional.of(javaTag));
        when(blogTagRepository.findByTagNameIgnoreCase("spring boot")).thenReturn(Optional.empty());
        when(blogTagRepository.save(any(BlogTag.class))).thenReturn(springTag);

        // Act
        BlogPost result = blogPostService.createPost("New Post", "Content", "Excerpt", "published", tagNames);

        // Assert
        assertNotNull(result);
        verify(blogPostRepository, atLeastOnce()).save(any(BlogPost.class));
        verify(blogTagRepository).findByTagNameIgnoreCase("java");
        verify(blogTagRepository).findByTagNameIgnoreCase("spring boot");
    }

    @Test
    void testCreatePost_WithoutTags_CreatesPostSuccessfully() {
        // Arrange
        BlogPost savedPost = new BlogPost();
        savedPost.setId(1L);
        savedPost.setTitle("New Post");

        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(savedPost);

        // Act
        BlogPost result = blogPostService.createPost("New Post", "Content", "Excerpt", "draft", null);

        // Assert
        assertNotNull(result);
        verify(blogPostRepository).save(any(BlogPost.class));
        verify(blogTagRepository, never()).findByTagNameIgnoreCase(anyString());
    }

    @Test
    void testUpdatePost_UpdatesAllFields() {
        // Arrange
        BlogPost existingPost = new BlogPost();
        existingPost.setId(1L);
        existingPost.setTitle("Old Title");
        existingPost.setBlogTags(new HashSet<>());

        Set<String> newTags = new HashSet<>(Arrays.asList("updated"));

        BlogTag updatedTag = new BlogTag();
        updatedTag.setId(3L);
        updatedTag.setTagName("updated");
        updatedTag.setBlogPost(existingPost);

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(existingPost));
        when(blogTagRepository.findByTagNameIgnoreCase("updated")).thenReturn(Optional.of(updatedTag));
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(existingPost);

        // Act
        BlogPost result = blogPostService.updatePost(1L, "New Title", "New Content", "New Excerpt", "published", newTags);

        // Assert
        assertNotNull(result);
        verify(blogPostRepository).findById(1L);
        verify(blogPostRepository).save(any(BlogPost.class));
    }

    @Test
    void testUpdatePost_NonExistentPost_ThrowsException() {
        // Arrange
        when(blogPostRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            blogPostService.updatePost(999L, "Title", "Content", "Excerpt", "draft", null);
        });
    }

    @Test
    void testDeletePost_WithMedia_DeletesFilesAndPost() {
        // Arrange
        BlogPost post = new BlogPost();
        post.setId(1L);

        BlogMedia media1 = new BlogMedia();
        media1.setId(1L);
        media1.setFilePath("images/test1.jpg");
        media1.setBlogPost(post);

        BlogMedia media2 = new BlogMedia();
        media2.setId(2L);
        media2.setExternalUrl("https://example.com/image.jpg");
        media2.setBlogPost(post);

        List<BlogMedia> mediaList = Arrays.asList(media1, media2);

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(1L)).thenReturn(mediaList);
        when(fileStorageService.deleteFile(anyString())).thenReturn(true);

        // Act
        blogPostService.deletePost(1L);

        // Assert
        verify(blogPostRepository).findById(1L);
        verify(blogMediaRepository).findByBlogPostIdOrderByDisplayOrderAsc(1L);
        verify(fileStorageService, times(1)).deleteFile(anyString()); // Only local file
        verify(blogPostRepository).delete(post);
    }

    @Test
    void testDeletePost_NonExistentPost_ThrowsException() {
        // Arrange
        when(blogPostRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            blogPostService.deletePost(999L);
        });
    }

    @Test
    void testSetCoverImage_ValidMedia_UpdatesCoverImage() {
        // Arrange
        BlogPost post = new BlogPost();
        post.setId(1L);

        BlogMedia oldCover = new BlogMedia();
        oldCover.setId(1L);
        oldCover.setDisplayOrder(0);
        oldCover.setBlogPost(post);

        BlogMedia newCover = new BlogMedia();
        newCover.setId(2L);
        newCover.setDisplayOrder(1);
        newCover.setBlogPost(post);

        List<BlogMedia> mediaList = Arrays.asList(oldCover, newCover);

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post));
        when(blogMediaRepository.findById(2L)).thenReturn(Optional.of(newCover));
        when(blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(1L)).thenReturn(mediaList);
        when(blogMediaRepository.save(any(BlogMedia.class))).thenReturn(newCover);
        when(blogPostRepository.save(any(BlogPost.class))).thenReturn(post);

        // Act
        BlogPost result = blogPostService.setCoverImage(1L, 2L);

        // Assert
        assertNotNull(result);
        verify(blogMediaRepository).save(oldCover);
        verify(blogMediaRepository).save(newCover);
    }

    @Test
    void testSetCoverImage_MediaBelongsToDifferentPost_ThrowsException() {
        // Arrange
        BlogPost post1 = new BlogPost();
        post1.setId(1L);

        BlogPost post2 = new BlogPost();
        post2.setId(2L);

        BlogMedia media = new BlogMedia();
        media.setId(1L);
        media.setBlogPost(post2); // Belongs to different post

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(post1));
        when(blogMediaRepository.findById(1L)).thenReturn(Optional.of(media));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            blogPostService.setCoverImage(1L, 1L);
        });
    }

    @Test
    void testGetPostsByTag_ReturnsFilteredPosts() {
        // Arrange
        List<BlogPost> posts = Arrays.asList(testPost);
        when(blogPostRepository.findByBlogTags_TagNameIgnoreCase("java")).thenReturn(posts);

        // Act
        List<BlogPost> result = blogPostService.getPostsByTag("Java");

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(blogPostRepository).findByBlogTags_TagNameIgnoreCase("java");
    }

    @Test
    void testGetAllTags_ReturnsAlphabeticallySortedTags() {
        // Arrange
        BlogTag tag1 = new BlogTag();
        tag1.setTagName("java");
        BlogTag tag2 = new BlogTag();
        tag2.setTagName("spring");

        when(blogTagRepository.findAllByOrderByTagNameAsc()).thenReturn(Arrays.asList(tag1, tag2));

        // Act
        List<BlogTag> result = blogPostService.getAllTags();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(blogTagRepository).findAllByOrderByTagNameAsc();
    }

    @Test
    void testGetAllPostsSorted_ReturnsPostsByPublishedDate() {
        // Arrange
        List<BlogPost> posts = Arrays.asList(testPost);
        when(blogPostRepository.findAllByOrderByPublishedAtDesc()).thenReturn(posts);

        // Act
        List<BlogPost> result = blogPostService.getAllPostsSorted();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(blogPostRepository).findAllByOrderByPublishedAtDesc();
    }
}
