package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public interface MoviePageProjection {
    Long getId();

    @JsonProperty("api_id")
    Long getApiId();

    String getTitle();

    Double getPopularity();

    @JsonProperty("poster_path")
    String getPosterPath();

    @JsonProperty("vote_average")
    Double getVoteAverage();

    Integer getYear();

    String getType();

    Integer getRuntime();

    @JsonIgnore
    LocalDate getReleaseDate();
}
