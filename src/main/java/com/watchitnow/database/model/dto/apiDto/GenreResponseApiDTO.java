package com.watchitnow.database.model.dto.apiDto;

import com.watchitnow.database.model.dto.databaseDto.GenreDTO;

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
