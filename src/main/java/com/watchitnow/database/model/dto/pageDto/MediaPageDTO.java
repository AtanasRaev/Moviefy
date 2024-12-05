package com.watchitnow.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class MediaPageDTO {
    private Long id;

    @JsonProperty("poster_path")
    private String posterPath;

    private String genre;

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

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }
}
