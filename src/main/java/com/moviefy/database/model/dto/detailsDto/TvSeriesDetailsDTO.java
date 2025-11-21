package com.moviefy.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.util.Set;

@JsonPropertyOrder({
        "id",
        "api_id",
        "imdb_id",
        "name",
        "original_name",
        "status",
        "poster_path",
        "backdrop_path",
        "first_air_date",
        "overview",
        "trailer",
        "episode_run_time",
        "vote_average",
        "api_id",
        "genres",
        "production_companies",
        "seasons"})
public class TvSeriesDetailsDTO extends MediaDetailsDTO {
    private String name;

    @JsonProperty("status")
    private String status;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("first_air_date")
    private LocalDate firstAirDate;

    private Set<SeasonTvSeriesDTO> seasons;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public LocalDate getFirstAirDate() {
        return firstAirDate;
    }

    public void setFirstAirDate(LocalDate firstAirDate) {
        this.firstAirDate = firstAirDate;
    }

    public Set<SeasonTvSeriesDTO> getSeasons() {
        return seasons;
    }

    public void setSeasons(Set<SeasonTvSeriesDTO> seasons) {
        this.seasons = seasons;
    }
}
