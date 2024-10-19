package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class MovieApiDTO extends MediaApiDTO{
    private String title;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    public MovieApiDTO() {
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}
