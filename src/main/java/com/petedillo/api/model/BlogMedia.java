package com.petedillo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

@Entity
@Table(name = "blog_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Nullable
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id", nullable = false)
    @NotNull
    private BlogPost blogPost;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 20, nullable = false)
    @NotNull
    private MediaType mediaType;

    @Column(name = "file_path", length = 500)
    @Nullable
    private String filePath;

    @Column(name = "external_url", columnDefinition = "TEXT")
    @Nullable
    private String externalUrl;

    @Column(name = "display_order")
    @Nullable
    private Integer displayOrder;

    @Column(name = "alt_text", length = 255)
    @Nullable
    private String altText;

    @Column(name = "caption", length = 500)
    @Nullable
    private String caption;

    @Column(name = "file_size")
    @Nullable
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    @Nullable
    private String mimeType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    @Nullable
    private LocalDateTime createdAt;

    public enum MediaType {
        IMAGE, VIDEO, AUDIO, EXTERNAL_IMAGE
    }

    /**
     * Get the URL for this media item.
     * For uploaded files: returns API path like /api/v1/media/images/uuid-file.jpg
     * For external images: returns external_url directly (e.g., Unsplash)
     */
    @Nullable
    public String getUrl() {
        if (externalUrl != null && !externalUrl.isEmpty()) {
            return externalUrl;
        }
        // Return API path for local files
        if (filePath != null && !filePath.isEmpty()) {
            return "/api/v1/media/" + filePath;
        }
        return null;
    }
    
    /**
     * Check if this media is stored locally (vs external URL)
     */
    public boolean isLocalFile() {
        return filePath != null && !filePath.isEmpty();
    }
}
