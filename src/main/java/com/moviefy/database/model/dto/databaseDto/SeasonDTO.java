package com.moviefy.database.model.dto.databaseDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class SeasonDTO {
    private Long id;

    @JsonProperty("air_date")
    private LocalDate airDate;

    @JsonProperty("episode_count")
    private Integer episodeCount;

    @JsonProperty("season_number")
    private Integer seasonNumber;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAirDate() {
        return airDate;
    }

    public void setAirDate(LocalDate airDate) {
        this.airDate = airDate;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public Integer getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(Integer seasonNumber) {
        this.seasonNumber = seasonNumber;
    }
}
