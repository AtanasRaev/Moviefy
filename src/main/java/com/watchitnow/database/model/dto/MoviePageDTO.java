package com.watchitnow.database.model.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MoviePageDTO {
    @JsonProperty("api_id")
    private Long apiId;

    private String title;

    @JsonProperty("poster_path")
    private String posterPath;

    private List<GenrePageDTO> genres;

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public List<GenrePageDTO> getGenres() {
        return genres;
    }

    public void setGenres(List<GenrePageDTO> genres) {
        this.genres = genres;
    }
}
