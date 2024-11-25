package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.Set;

public abstract class MediaApiDTO {
    private Long id;

    @NotBlank
    private String overview;

    @NotNull
    @Positive
    private Double popularity;

    @JsonProperty("genre_ids")
    private Set<Long> genres;

    @NotBlank
    @JsonProperty("poster_path")
    private String posterPath;

    @NotBlank
    @JsonProperty("backdrop_path")
    private String backdropPath;

    @NotNull
    @Positive
    @JsonProperty("vote_average")
    private Double voteAverage;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public Set<Long> getGenres() {
        return genres;
    }

    public void setGenres(Set<Long> genres) {
        this.genres = genres;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }
}
