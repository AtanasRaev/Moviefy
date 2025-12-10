package com.moviefy.database.model.dto.pageDto.mediaDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieHomeDTO extends MovieDTO{
    private String trailer;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }
}
