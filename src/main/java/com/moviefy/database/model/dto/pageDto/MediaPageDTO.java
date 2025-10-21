package com.moviefy.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MediaPageDTO {
    private Long id;

    @JsonProperty("vote_average")
    private Double voteAverage;

    // Used for internal sorting/merging only; do not serialize in API responses
    @JsonIgnore
    private Double popularity;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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
}
