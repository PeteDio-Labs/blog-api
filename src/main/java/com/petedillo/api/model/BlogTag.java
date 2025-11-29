package com.petedillo.api.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Setter
@Getter
@Entity
@Table(name = "blog_tags")
public class BlogTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @Column(name = "tag_name", nullable = false, length = 50)
    @Nullable
    private String tagName;

    @Column(name = "created_at", nullable = false)
    @Nullable
    private LocalDateTime createdAt;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id", nullable = false)
    @NotNull
    private BlogPost blogPost;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        // Ensure tag is lowercase per database constraint
        if (tagName != null) {
            tagName = tagName.toLowerCase();
        }
    }
}
