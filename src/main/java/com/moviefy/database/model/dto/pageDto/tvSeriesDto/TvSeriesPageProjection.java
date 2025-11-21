package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public interface TvSeriesPageProjection {
    Long getId();

    @JsonProperty("api_id")
    Long getApiId();

    String getName();

    Double getPopularity();

    @JsonProperty("poster_path")
    String getPosterPath();

    @JsonProperty("vote_average")
    Double getVoteAverage();

    Integer getYear();

    @JsonProperty("media_type")
    String getMediaType();

    @JsonProperty("seasons")
    Integer getSeasonsCount();

    @JsonProperty("episodes")
    Integer getEpisodesCount();

    @JsonIgnore
    LocalDate getReleaseDate();
}
