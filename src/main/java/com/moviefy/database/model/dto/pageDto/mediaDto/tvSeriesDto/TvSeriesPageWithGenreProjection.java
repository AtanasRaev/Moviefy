package com.moviefy.database.model.dto.pageDto.mediaDto.tvSeriesDto;

public interface TvSeriesPageWithGenreProjection extends TvSeriesPageProjection {
    String getGenre();

    String getTrailer();

    Double getScore();
}
