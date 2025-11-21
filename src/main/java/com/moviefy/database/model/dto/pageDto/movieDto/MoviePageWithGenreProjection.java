package com.moviefy.database.model.dto.pageDto.movieDto;

public interface MoviePageWithGenreProjection extends MoviePageProjection {
    String getGenre();

    String getTrailer();
}
