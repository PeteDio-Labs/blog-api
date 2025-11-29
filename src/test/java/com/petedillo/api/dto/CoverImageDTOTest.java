package com.petedillo.api.dto;

import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CoverImageDTOTest {

    @Test
    void testFromEntity_ValidCoverImage_MapsCorrectly() {
        // Arrange
        BlogPost post = new BlogPost();
        BlogMedia coverImage = new BlogMedia();
        coverImage.setBlogPost(post);
        coverImage.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        coverImage.setExternalUrl("https://example.com/cover.jpg");
        coverImage.setDisplayOrder(0);
        coverImage.setAltText("Cover image");

        // Act
        CoverImageDTO dto = CoverImageDTO.fromEntity(coverImage);

        // Assert
        assertNotNull(dto);
        assertEquals("https://example.com/cover.jpg", dto.getUrl());
        assertEquals("Cover image", dto.getAltText());
    }

    @Test
    void testFromEntity_LocalFile_MapsWithApiPath() {
        // Arrange
        BlogPost post = new BlogPost();
        BlogMedia coverImage = new BlogMedia();
        coverImage.setBlogPost(post);
        coverImage.setMediaType(BlogMedia.MediaType.IMAGE);
        coverImage.setFilePath("images/cover-123.jpg");
        coverImage.setAltText("Local cover");

        // Act
        CoverImageDTO dto = CoverImageDTO.fromEntity(coverImage);

        // Assert
        assertNotNull(dto);
        assertEquals("/api/v1/media/images/cover-123.jpg", dto.getUrl());
        assertEquals("Local cover", dto.getAltText());
    }

    @Test
    void testFromEntity_NullMedia_ReturnsNull() {
        // Act
        CoverImageDTO dto = CoverImageDTO.fromEntity(null);

        // Assert
        assertNull(dto);
    }

    @Test
    void testFromEntity_NullAltText_HandlesGracefully() {
        // Arrange
        BlogPost post = new BlogPost();
        BlogMedia coverImage = new BlogMedia();
        coverImage.setBlogPost(post);
        coverImage.setMediaType(BlogMedia.MediaType.EXTERNAL_IMAGE);
        coverImage.setExternalUrl("https://example.com/cover.jpg");
        // No altText

        // Act
        CoverImageDTO dto = CoverImageDTO.fromEntity(coverImage);

        // Assert
        assertNotNull(dto);
        assertEquals("https://example.com/cover.jpg", dto.getUrl());
        assertNull(dto.getAltText());
    }
}
