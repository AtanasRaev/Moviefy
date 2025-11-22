package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface CollectionPageProjection {
    String getName();

    @JsonProperty("poster_path")
    String getPosterPath();

    @JsonProperty("api_id")
    Long getApiId();

    String getOverview();

    Integer getRuntime();

    @JsonProperty("vote_average")
    Double getVoteAverage();
}
