package com.watchitnow.database.model.dto;

import java.util.List;

public class GenreResponseApiDTO {
    private List<GenreDTO> genres;

    public List<GenreDTO> getGenres() {
        return genres;
    }

    public void setGenres(List<GenreDTO> genres) {
        this.genres = genres;
    }
}
