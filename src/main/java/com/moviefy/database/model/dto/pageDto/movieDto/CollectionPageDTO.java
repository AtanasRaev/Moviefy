package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionPageDTO {
    private String name;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("first_movie_id")
    private Long firstMovieId;

    private String overview;

    private Integer runtime;

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

    public Long getFirstMovieId() {
        return firstMovieId;
    }

    public void setFirstMovieId(Long firstMovieId) {
        this.firstMovieId = firstMovieId;
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
}
