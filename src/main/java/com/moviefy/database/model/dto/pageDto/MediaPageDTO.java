package com.moviefy.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MediaPageDTO {
    private Long id;

    @JsonProperty("vote_average")
    private Double voteAverage;

    @JsonIgnore
    private Double popularity;

    @JsonProperty("api_id")
    private Long apiId;

    @JsonProperty("imdb_id")
    private String imdbId;

    public MediaPageDTO(Long id, Double voteAverage, Double popularity, Long apiId) {
        this.id = id;
        this.voteAverage = voteAverage;
        this.popularity = popularity;
        this.apiId = apiId;
    }

    public MediaPageDTO() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public abstract String getMediaType();

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }
}
