package com.moviefy.database.model.dto.apiDto;

import com.moviefy.database.model.dto.databaseDto.GenreDTO;

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
