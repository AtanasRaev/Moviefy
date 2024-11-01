package com.watchitnow.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public abstract class MediaPageDTO {
    private Long id;

    @JsonProperty("poster_path")
    private String posterPath;

    private List<GenrePageDTO> genres;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
