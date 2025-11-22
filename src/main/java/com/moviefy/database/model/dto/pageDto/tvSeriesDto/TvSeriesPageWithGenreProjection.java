package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

public interface TvSeriesPageWithGenreProjection extends TvSeriesPageProjection {
    String getGenre();

    String getTrailer();

    Double getScore();
}
