package com.petedillo.api.dto;

import com.petedillo.api.model.PostStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for blog post creation and update requests.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostRequest {
    private String title;
    private String content;
    private String excerpt;
    private PostStatus status;
    private List<String> tags;
}
