package com.petedillo.api.service;

import com.petedillo.api.dto.MediaDTO;
import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.repository.BlogMediaRepository;
import com.petedillo.api.repository.BlogPostRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MediaServiceTest {

    @Mock
    private BlogMediaRepository blogMediaRepository;

    @Mock
    private BlogPostRepository blogPostRepository;

    @Mock
    private FileStorageService fileStorageService;

    @InjectMocks
    private MediaService mediaService;

    private BlogPost testPost;
    private BlogMedia testMedia;

    @BeforeEach
    void setUp() {
        testPost = new BlogPost();
        testPost.setId(1L);
        testPost.setTitle("Test Post");

        testMedia = new BlogMedia();
        testMedia.setId(1L);
        testMedia.setBlogPost(testPost);
        testMedia.setMediaType(BlogMedia.MediaType.IMAGE);
        testMedia.setFilePath("images/test.jpg");
        testMedia.setDisplayOrder(0);
        testMedia.setAltText("Test image");
    }

    @Test
    void testUploadMedia_ValidFile_UploadsSuccessfully() throws IOException {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );

        when(blogPostRepository.findById(1L)).thenReturn(Optional.of(testPost));
        when(fileStorageService.storeFile(file)).thenReturn("images/stored-file.jpg");
        when(blogMediaRepository.countByBlogPost(testPost)).thenReturn(0L);
        when(blogMediaRepository.save(any(BlogMedia.class))).thenReturn(testMedia);

        // Act
        MediaDTO result = mediaService.uploadMedia(file, 1L, "Alt text", "Caption");

        // Assert
        assertNotNull(result);
        verify(blogPostRepository).findById(1L);
        verify(fileStorageService).storeFile(file);
        verify(blogMediaRepository).save(any(BlogMedia.class));
    }

    @Test
    void testUploadMedia_NonExistentPost_ThrowsException() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
            "test.jpg",
            "test.jpg",
            "image/jpeg",
            "test content".getBytes()
        );

        when(blogPostRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            mediaService.uploadMedia(file, 999L, "Alt text", "Caption");
        });
    }

    @Test
    void testGetMediaForPost_ReturnsMediaList() {
        // Arrange
        List<BlogMedia> mediaList = Arrays.asList(testMedia);
        when(blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(1L)).thenReturn(mediaList);

        // Act
        List<MediaDTO> result = mediaService.getMediaForPost(1L);

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(blogMediaRepository).findByBlogPostIdOrderByDisplayOrderAsc(1L);
    }

    @Test
    void testGetAllMedia_ReturnsAllMedia() {
        // Arrange
        List<BlogMedia> mediaList = Arrays.asList(testMedia);
        when(blogMediaRepository.findAllByOrderByCreatedAtDesc()).thenReturn(mediaList);

        // Act
        List<MediaDTO> result = mediaService.getAllMedia();

        // Assert
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(blogMediaRepository).findAllByOrderByCreatedAtDesc();
    }

    @Test
    void testDeleteMedia_LocalFile_DeletesFileAndEntity() {
        // Arrange
        when(blogMediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));
        when(fileStorageService.deleteFile(anyString())).thenReturn(true);

        // Act
        mediaService.deleteMedia(1L);

        // Assert
        verify(blogMediaRepository).findById(1L);
        verify(fileStorageService).deleteFile(anyString());
        verify(blogMediaRepository).delete(testMedia);
    }

    @Test
    void testDeleteMedia_ExternalUrl_DeletesOnlyEntity() {
        // Arrange
        testMedia.setFilePath(null);
        testMedia.setExternalUrl("https://example.com/image.jpg");

        when(blogMediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));

        // Act
        mediaService.deleteMedia(1L);

        // Assert
        verify(blogMediaRepository).findById(1L);
        verify(fileStorageService, never()).deleteFile(anyString());
        verify(blogMediaRepository).delete(testMedia);
    }

    @Test
    void testDeleteMedia_NonExistentMedia_ThrowsException() {
        // Arrange
        when(blogMediaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            mediaService.deleteMedia(999L);
        });
    }

    @Test
    void testReorderMedia_ValidOrder_UpdatesDisplayOrder() {
        // Arrange
        BlogMedia media1 = new BlogMedia();
        media1.setId(1L);
        media1.setBlogPost(testPost);
        media1.setDisplayOrder(0);

        BlogMedia media2 = new BlogMedia();
        media2.setId(2L);
        media2.setBlogPost(testPost);
        media2.setDisplayOrder(1);

        when(blogMediaRepository.findById(1L)).thenReturn(Optional.of(media1));
        when(blogMediaRepository.findById(2L)).thenReturn(Optional.of(media2));
        when(blogMediaRepository.saveAll(anyList())).thenReturn(Arrays.asList(media1, media2));

        // Act
        mediaService.reorderMedia(1L, Arrays.asList(2L, 1L));

        // Assert
        verify(blogMediaRepository).saveAll(anyList());
    }

    @Test
    void testReorderMedia_MediaFromDifferentPost_ThrowsException() {
        // Arrange
        BlogPost otherPost = new BlogPost();
        otherPost.setId(2L);

        BlogMedia media1 = new BlogMedia();
        media1.setId(1L);
        media1.setBlogPost(otherPost); // Different post

        when(blogMediaRepository.findById(1L)).thenReturn(Optional.of(media1));

        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            mediaService.reorderMedia(1L, Arrays.asList(1L));
        });
    }

    @Test
    void testReorderMedia_NonExistentPost_ThrowsException() {
        // Arrange
        when(blogMediaRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            mediaService.reorderMedia(999L, Arrays.asList(1L));
        });
    }

    @Test
    void testUpdateMetadata_UpdatesAltTextAndCaption() {
        // Arrange
        when(blogMediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));
        when(blogMediaRepository.save(any(BlogMedia.class))).thenReturn(testMedia);

        // Act
        MediaDTO result = mediaService.updateMetadata(1L, "New alt text", "New caption");

        // Assert
        assertNotNull(result);
        verify(blogMediaRepository).findById(1L);
        verify(blogMediaRepository).save(testMedia);
    }

    @Test
    void testUpdateMetadata_NullValues_UpdatesOnlyNonNull() {
        // Arrange
        when(blogMediaRepository.findById(1L)).thenReturn(Optional.of(testMedia));
        when(blogMediaRepository.save(any(BlogMedia.class))).thenReturn(testMedia);

        // Act
        MediaDTO result = mediaService.updateMetadata(1L, "New alt text", null);

        // Assert
        assertNotNull(result);
        verify(blogMediaRepository).save(testMedia);
    }

    @Test
    void testUpdateMetadata_NonExistentMedia_ThrowsException() {
        // Arrange
        when(blogMediaRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            mediaService.updateMetadata(999L, "Alt text", "Caption");
        });
    }
}
