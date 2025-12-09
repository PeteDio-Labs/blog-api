package com.petedillo.api.dto;

import com.petedillo.api.model.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for blog post responses.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostResponse {
    private Long id;
    private String title;
    private String content;
    private String excerpt;
    private PostStatus status;
    private List<String> tags;
    private String authorName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private LocalDateTime publishedAt;
}
