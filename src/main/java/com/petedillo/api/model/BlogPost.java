package com.petedillo.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.petedillo.api.dto.CoverImageDTO;
import com.petedillo.api.dto.MediaDTO;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "BlogPost.tags",
        attributeNodes = @NamedAttributeNode("tags")
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

    @ManyToMany(cascade = {CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
        name = "post_tags",
        joinColumns = @JoinColumn(name = "post_id"),
        inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private Set<Tag> tags = new HashSet<>();

    @OneToMany(mappedBy = "blogPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<BlogMedia> media = new ArrayList<>();

    // Convenience method to get tag names as List<String> for JSON serialization
    @JsonProperty("tags")
    public List<String> getTagNames() {
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

    // Convenience method to set tags from List<String>
    public void setTagNames(@Nullable List<String> tagNames) {
        this.tags = new HashSet<>();
        if (tagNames != null) {
            for (String tagName : tagNames) {
                if (tagName != null && !tagName.isEmpty()) {
                    Tag tag = new Tag();
                    tag.setName(tagName.toLowerCase());
                    tag.setSlug(tagName.toLowerCase().replaceAll("\\s+", "-"));
                    tag.setPostCount(1);
                    this.tags.add(tag);
                }
            }
        }
    }

    // Backward compatibility: accept List<String> and convert to Set
    public void setTags(@Nullable List<String> tagNames) {
        setTagNames(tagNames);
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
