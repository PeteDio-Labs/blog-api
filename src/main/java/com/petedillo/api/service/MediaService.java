package com.petedillo.api.service;

import com.petedillo.api.dto.MediaDTO;
import com.petedillo.api.exception.MediaUploadException;
import com.petedillo.api.exception.ResourceNotFoundException;
import com.petedillo.api.model.BlogMedia;
import com.petedillo.api.model.BlogPost;
import com.petedillo.api.repository.BlogMediaRepository;
import com.petedillo.api.repository.BlogPostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MediaService {

    @Autowired
    private BlogMediaRepository blogMediaRepository;

    @Autowired
    private BlogPostRepository blogPostRepository;

    @Autowired
    private FileStorageService fileStorageService;

    /**
     * Upload a media file and associate it with a blog post
     */
    @Transactional
    public MediaDTO uploadMedia(MultipartFile file, Long postId, String altText, String caption) {
        try {
            // Validate post exists
            BlogPost blogPost = blogPostRepository.findById(postId)
                .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + postId));

            // Store file using FileStorageService (validates file type)
            String filePath = fileStorageService.storeFile(file);

            // Create BlogMedia entity
            BlogMedia media = new BlogMedia();
            media.setBlogPost(blogPost);
            media.setMediaType(BlogMedia.MediaType.IMAGE);
            media.setFilePath(filePath);
            media.setAltText(altText != null ? altText : file.getOriginalFilename());
            media.setCaption(caption);
            media.setFileSize(file.getSize());
            media.setMimeType(file.getContentType());

            // Set display order (add to end)
            long existingCount = blogMediaRepository.countByBlogPost(blogPost);
            media.setDisplayOrder((int) existingCount);

            BlogMedia savedMedia = blogMediaRepository.save(media);
            return MediaDTO.fromEntity(savedMedia);

        } catch (IOException e) {
            throw new MediaUploadException("Failed to upload media file", e);
        }
    }

    /**
     * Get all media for a specific post
     */
    public List<MediaDTO> getMediaForPost(Long postId) {
        List<BlogMedia> mediaList = blogMediaRepository.findByBlogPostIdOrderByDisplayOrderAsc(postId);
        return mediaList.stream()
            .map(MediaDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Get all media across all posts (for media manager)
     */
    public List<MediaDTO> getAllMedia() {
        List<BlogMedia> mediaList = blogMediaRepository.findAllByOrderByCreatedAtDesc();
        return mediaList.stream()
            .map(MediaDTO::fromEntity)
            .collect(Collectors.toList());
    }

    /**
     * Delete a media file (removes both file and database entry)
     */
    @Transactional
    public void deleteMedia(Long mediaId) {
        BlogMedia media = blogMediaRepository.findById(mediaId)
            .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));

        // Delete physical file if it's a local file
        if (media.isLocalFile()) {
            String filename = media.getFilePath().substring(media.getFilePath().lastIndexOf('/') + 1);
            fileStorageService.deleteFile(filename);
        }

        // Delete from database
        blogMediaRepository.delete(media);
    }

    /**
     * Reorder media items for a post (validates all media belong to the post)
     */
    @Transactional
    public void reorderMedia(Long postId, List<Long> mediaIds) {
        // Validate post exists
        BlogPost blogPost = blogPostRepository.findById(postId)
            .orElseThrow(() -> new ResourceNotFoundException("Blog post not found: " + postId));

        // Validate all media items exist and belong to this post
        for (int i = 0; i < mediaIds.size(); i++) {
            Long mediaId = mediaIds.get(i);
            BlogMedia media = blogMediaRepository.findById(mediaId)
                .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));

            // Validate media belongs to this post
            if (!media.getBlogPost().getId().equals(postId)) {
                throw new IllegalArgumentException(
                    "Media item " + mediaId + " does not belong to post " + postId
                );
            }

            // Update display order
            media.setDisplayOrder(i);
            blogMediaRepository.save(media);
        }
    }

    /**
     * Update media metadata (alt text and caption)
     */
    @Transactional
    public MediaDTO updateMetadata(Long mediaId, String altText, String caption) {
        BlogMedia media = blogMediaRepository.findById(mediaId)
            .orElseThrow(() -> new ResourceNotFoundException("Media not found: " + mediaId));

        if (altText != null) {
            media.setAltText(altText);
        }
        if (caption != null) {
            media.setCaption(caption);
        }

        BlogMedia updatedMedia = blogMediaRepository.save(media);
        return MediaDTO.fromEntity(updatedMedia);
    }
}
