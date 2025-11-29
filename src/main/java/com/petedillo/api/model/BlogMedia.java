package com.petedillo.api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "blog_media")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BlogMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blog_post_id", nullable = false)
    private BlogPost blogPost;

    @Enumerated(EnumType.STRING)
    @Column(name = "media_type", length = 20, nullable = false)
    private MediaType mediaType;

    @Column(name = "file_path", length = 500)
    private String filePath;

    @Column(name = "external_url", columnDefinition = "TEXT")
    private String externalUrl;

    @Column(name = "display_order")
    private Integer displayOrder;

    @Column(name = "alt_text", length = 255)
    private String altText;

    @Column(name = "caption", length = 500)
    private String caption;

    @Column(name = "file_size")
    private Long fileSize;

    @Column(name = "mime_type", length = 100)
    private String mimeType;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum MediaType {
        IMAGE, VIDEO, AUDIO, EXTERNAL_IMAGE
    }

    /**
     * Get the URL for this media item.
     * For uploaded files: returns API path like /api/v1/media/images/uuid-file.jpg
     * For external images: returns external_url directly (e.g., Unsplash)
     */
    public String getUrl() {
        if (externalUrl != null && !externalUrl.isEmpty()) {
            return externalUrl;
        }
        // Return API path for local files
        return "/api/v1/media/" + filePath;
    }
    
    /**
     * Check if this media is stored locally (vs external URL)
     */
    public boolean isLocalFile() {
        return filePath != null && !filePath.isEmpty();
    }
}
