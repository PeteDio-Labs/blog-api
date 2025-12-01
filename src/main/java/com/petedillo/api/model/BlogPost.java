package com.petedillo.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.petedillo.api.dto.CoverImageDTO;
import com.petedillo.api.dto.MediaDTO;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "BlogPost.tags",
        attributeNodes = @NamedAttributeNode("blogTags")
    ),
    @NamedEntityGraph(
        name = "BlogPost.media",
        attributeNodes = @NamedAttributeNode("media")
    )
})
@Setter
@Getter
@Entity
@Table(name = "blog_posts")
public class BlogPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @NotNull
    private String title;

    @NotNull
    private String slug;

    @NotNull
    private String content;

    @Nullable
    private String excerpt;

    @NotNull
    private String status;

    @Column(name = "is_featured")
    private boolean isFeatured;

    @Column(name = "view_count")
    private int viewCount;

    @Column(name = "created_at")
    @Nullable
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    @Nullable
    private LocalDateTime updatedAt;

    @Column(name = "published_at")
    @Nullable
    private LocalDateTime publishedAt;

    @JsonIgnore
    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private Set<BlogTag> blogTags = new HashSet<>();

    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    @Getter(AccessLevel.NONE)
    private List<BlogMedia> media = new ArrayList<>();

    /**
     * Returns an unmodifiable view of the media list to prevent external modification.
     * Use {@link #addMedia(BlogMedia)} to add media items.
     */
    public List<BlogMedia> getMedia() {
        return Collections.unmodifiableList(media);
    }

    /**
     * Adds a media item to this blog post and sets the bidirectional relationship.
     * @param mediaItem the media item to add (must not be null)
     * @throws IllegalArgumentException if mediaItem is null
     */
    public void addMedia(BlogMedia mediaItem) {
        if (mediaItem == null) {
            throw new IllegalArgumentException("mediaItem must not be null");
        }
        mediaItem.setBlogPost(this);
        media.add(mediaItem);
    }

    // Convenience method to get tag names as List<String> for JSON serialization
    @JsonProperty("tags")
    public List<String> getTags() {
        return blogTags.stream()
                .map(BlogTag::getTagName)
                .collect(Collectors.toList());
    }

    // Convenience method to set tags from List<String>
    public void setTags(@Nullable List<String> tags) {
        this.blogTags = new HashSet<>();
        if (tags != null) {
            for (String tag : tags) {
                if (tag != null) {
                    BlogTag blogTag = new BlogTag();
                    blogTag.setTagName(tag.toLowerCase());
                    blogTag.setBlogPost(this);
                    this.blogTags.add(blogTag);
                }
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
    @Nullable
    public CoverImageDTO getCoverImage() {
        return media.stream()
                .filter(m -> m.getDisplayOrder() != null && m.getDisplayOrder() == 0)
                .findFirst()
                .map(CoverImageDTO::fromEntity)
                .orElse(null);
    }

}
