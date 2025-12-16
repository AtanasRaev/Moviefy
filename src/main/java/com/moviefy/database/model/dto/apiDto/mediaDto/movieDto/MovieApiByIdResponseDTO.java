package com.moviefy.database.model.dto.apiDto.mediaDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.apiDto.CollectionApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.MediaApiByIdResponseDTO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public class MovieApiByIdResponseDTO extends MediaApiByIdResponseDTO {
    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    @JsonProperty("belongs_to_collection")
    private CollectionApiDTO collection;

    @NotNull
    @Positive
    private Integer runtime;

    @JsonProperty("imdb_id")
    private String imdbId;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public CollectionApiDTO getCollection() {
        return collection;
    }

    public void setCollection(CollectionApiDTO collection) {
        this.collection = collection;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }
}