package com.moviefy.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class SeasonTvSeriesDTO {
    @JsonProperty("air_date")
    private LocalDate airDate;

    @JsonProperty("episode_count")
    private Integer episodeCount;

    @JsonProperty("season_number")
    private Integer seasonNumber;

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
