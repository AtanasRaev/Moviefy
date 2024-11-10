package com.watchitnow.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Set;

public abstract class MediaPageDTO {
    private Long id;

    @JsonProperty("poster_path")
    private String posterPath;

    private Set<GenrePageDTO> genres;

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

    public Set<GenrePageDTO> getGenres() {
        return genres;
    }

    public void setGenres(Set<GenrePageDTO> genres) {
        this.genres = genres;
    }
}
