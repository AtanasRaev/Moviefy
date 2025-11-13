package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionPageDTO {
    private String name;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("first_movie_api_id")
    private Long firstMovieApiId;

    private String overview;

    private Integer runtime;

    private Double voteAverage;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Long getFirstMovieApiId() {
        return firstMovieApiId;
    }

    public void setFirstMovieApiId(Long firstMovieApiId) {
        this.firstMovieApiId = firstMovieApiId;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }
}
