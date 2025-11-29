package com.petedillo.api.dto;

import com.petedillo.api.model.BlogMedia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MediaDTO {
    @Nullable
    private Long id;
    @Nullable
    private String type;
    @Nullable
    private String url;
    @Nullable
    private String altText;
    @Nullable
    private String caption;
    @Nullable
    private Integer displayOrder;

    @Nullable
    public static MediaDTO fromEntity(@Nullable BlogMedia media) {
        if (media == null) return null;

        return new MediaDTO(
            media.getId(),
            media.getMediaType() != null ? media.getMediaType().name() : null,
            media.getUrl(), // Uses getUrl() helper method
            media.getAltText(),
            media.getCaption(),
            media.getDisplayOrder()
        );
    }
}
