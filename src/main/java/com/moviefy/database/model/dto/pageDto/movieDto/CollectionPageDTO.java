package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionPageDTO {
    private String name;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("first_movie_id")
    private Long firstMovieId;

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
}
