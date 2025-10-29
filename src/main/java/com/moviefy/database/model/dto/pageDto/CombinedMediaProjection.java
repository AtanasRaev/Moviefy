package com.moviefy.database.model.dto.pageDto;

public interface CombinedMediaProjection {
    Long getId();
    String getTitle();
    Double getPopularity();
    String getPosterPath();
    Double getVoteAverage();
    Integer getYear();
    String getType();
    Integer getSeasonsCount();
    Integer getEpisodesCount();
}
