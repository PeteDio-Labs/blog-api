package com.petedillo.api.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NamedEntityGraphs({
    @NamedEntityGraph(
        name = "BlogPost.tags",
        attributeNodes = @NamedAttributeNode("tags")
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

    @JsonProperty("tags")
    public List<String> getTagNames() {
        return tags.stream()
                .map(Tag::getName)
                .collect(Collectors.toList());
    }

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

    public void setTags(@Nullable List<String> tagNames) {
        setTagNames(tagNames);
    }
}
