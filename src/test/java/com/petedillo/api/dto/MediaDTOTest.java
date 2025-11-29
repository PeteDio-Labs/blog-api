package com.petedillo.api.dto;

import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MediaDTOTest {

    @Test
    void testFromEntity_ExternalImage_MapsCorrectly() {
        // Arrange
        BlogPost post = new BlogPost();
        post.setId(1L);
        
        BlogMedia media = new BlogMedia();
        media.setId(1L);
        media.setBlogPost(post);
        media.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        media.setExternalUrl("https://example.com/image.jpg");
        media.setDisplayOrder(0);
        media.setAltText("Test image");
        media.setCaption("Test caption");

        // Act
        MediaDTO dto = MediaDTO.fromEntity(media);

        // Assert
        assertNotNull(dto);
        assertEquals(1L, dto.getId());
        assertEquals("EXTERNAL_IMAGE", dto.getType());
        assertEquals("https://example.com/image.jpg", dto.getUrl());
        assertEquals("Test image", dto.getAltText());
        assertEquals("Test caption", dto.getCaption());
        assertEquals(0, dto.getDisplayOrder());
    }

    @Test
    void testFromEntity_LocalImage_MapsWithApiPath() {
        // Arrange
        BlogPost post = new BlogPost();
        BlogMedia media = new BlogMedia();
        media.setId(2L);
        media.setBlogPost(post);
        media.setMediaType(BlogMedia.MediaType.IMAGE);
        media.setFilePath("images/test-123.jpg");
        media.setDisplayOrder(1);
        media.setAltText("Local image");

        // Act
        MediaDTO dto = MediaDTO.fromEntity(media);

        // Assert
        assertNotNull(dto);
        assertEquals(2L, dto.getId());
        assertEquals("IMAGE", dto.getType());
        assertEquals("/api/v1/media/images/test-123.jpg", dto.getUrl());
        assertEquals("Local image", dto.getAltText());
        assertEquals(1, dto.getDisplayOrder());
    }

    @Test
    void testFromEntity_NullMedia_ReturnsNull() {
        // Act
        MediaDTO dto = MediaDTO.fromEntity(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void testFromEntity_NullOptionalFields_HandlesGracefully() {
        // Arrange
        BlogPost post = new BlogPost();
        BlogMedia media = new BlogMedia();
        media.setId(3L);
        media.setBlogPost(post);
        media.setMediaType(BlogMedia.MediaType.VIDEO);
        media.setExternalUrl("https://example.com/video.mp4");
        media.setDisplayOrder(0);
        // No altText or caption

        // Act
        MediaDTO dto = MediaDTO.fromEntity(media);

        // Assert
        assertNotNull(dto);
        assertEquals(3L, dto.getId());
        assertNull(dto.getAltText());
        assertNull(dto.getCaption());
    }
}
