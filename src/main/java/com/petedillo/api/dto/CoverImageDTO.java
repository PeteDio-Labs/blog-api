package com.petedillo.api.dto;

import com.petedillo.api.model.BlogMedia;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jetbrains.annotations.Nullable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CoverImageDTO {
    @Nullable
    private String url;
    @Nullable
    private String altText;

    @Nullable
    public static CoverImageDTO fromEntity(@Nullable BlogMedia media) {
        if (media == null) return null;
        return new CoverImageDTO(media.getUrl(), media.getAltText());
    }
}
