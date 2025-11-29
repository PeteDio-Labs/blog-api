package com.petedillo.api.model;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.List;

@Setter
@Getter
public class SearchResponseDTO {
    @NotNull
    private String searchTerm;
    @NotNull
    private List<BlogPost> results;

    public SearchResponseDTO(@NotNull String searchTerm, @NotNull List<BlogPost> results) {
        this.searchTerm = searchTerm;
        this.results = results;
    }

}
