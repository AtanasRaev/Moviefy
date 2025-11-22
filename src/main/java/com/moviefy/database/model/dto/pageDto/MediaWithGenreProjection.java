package com.moviefy.database.model.dto.pageDto;

public interface MediaWithGenreProjection extends MediaProjection {
    String getGenre();

    String getTrailer();

    Double getScore();
}
