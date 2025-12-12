package com.petedillo.api.dto;

import com.petedillo.api.model.Tag;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response DTO for Tag data.
 * Used in admin endpoints to provide tag information.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TagResponse {

    /**
     * Tag name.
     */
    private String name;

    /**
     * URL-friendly slug.
     */
    private String slug;

    /**
     * Number of posts associated with this tag.
     */
    private Integer postCount;

    /**
     * Convert Tag entity to TagResponse DTO.
     *
     * @param tag the tag entity
     * @return TagResponse DTO
     */
    public static TagResponse fromEntity(Tag tag) {
        return new TagResponse(
                tag.getName(),
                tag.getSlug(),
                tag.getPostCount()
        );
    }
}
