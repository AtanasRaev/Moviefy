package com.moviefy.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class SeasonTvSeriesDTO {
    private Long id;

    @JsonProperty("air_date")
    private LocalDate airDate;

    @JsonProperty("episode_count")
    private Integer episodeCount;

    @JsonProperty("season_number")
    private Integer seasonNumber;

    @JsonProperty("poster_path")
    private String posterPath;


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

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
