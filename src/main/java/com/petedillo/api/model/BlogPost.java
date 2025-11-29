package com.petedillo.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petedillo.api.dto.CoverImageDTO;
import com.petedillo.api.dto.MediaDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Setter
@Getter
@Entity
@Table(name = "blog_posts")
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;
    private String slug;
    private String content;
    private String excerpt;
    private String status;

    @Column(name = "is_featured")
    private boolean isFeatured;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<BlogTag> blogTags = new HashSet<>();

    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<BlogMedia> media = new ArrayList<>();

    // Convenience method to get tag names as List<String> for JSON serialization
    @JsonProperty("tags")
    public List<String> getTags() {
        return blogTags.stream()
                .map(BlogTag::getTagName)
                .collect(Collectors.toList());
    }

    // Convenience method to set tags from List<String>
    public void setTags(List<String> tags) {
        this.blogTags = new HashSet<>();
        if (tags != null) {
            for (String tag : tags) {
                BlogTag blogTag = new BlogTag();
                blogTag.setTagName(tag.toLowerCase());
                blogTag.setBlogPost(this);
                this.blogTags.add(blogTag);
            }
        }
    }

    // Get media items as DTOs for JSON serialization
    @JsonProperty("media")
    public List<MediaDTO> getMediaDTOs() {
        return media.stream()
                .map(MediaDTO::fromEntity)
                .collect(Collectors.toList());
    }

    // Get cover image (first media with displayOrder = 0)
    @JsonProperty("coverImage")
    public CoverImageDTO getCoverImage() {
        return media.stream()
                .filter(m -> m.getDisplayOrder() != null && m.getDisplayOrder() == 0)
                .findFirst()
                .map(CoverImageDTO::fromEntity)
                .orElse(null);
    }

}
