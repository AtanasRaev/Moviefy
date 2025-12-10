package com.moviefy.database.model.dto.pageDto.mediaDto.movieDto;

public interface MoviePageWithGenreProjection extends MoviePageProjection {
    String getGenre();

    String getTrailer();

    Double getScore();
}
