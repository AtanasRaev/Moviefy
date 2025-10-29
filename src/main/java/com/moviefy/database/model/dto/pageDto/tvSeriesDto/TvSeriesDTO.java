package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.pageDto.MediaPageDTO;

public abstract class TvSeriesDTO extends MediaPageDTO {
    private String name;

    @JsonProperty("seasons")
    private Integer seasonsCount;

    @JsonProperty("episodes")
    private Integer episodesCount;

    public TvSeriesDTO(Long id, Double voteAverage, Double popularity, String name, Integer seasonsCount, Integer episodesCount) {
        super(id, voteAverage, popularity);
        this.name = name;
        this.seasonsCount = seasonsCount;
        this.episodesCount = episodesCount;
    }

    public TvSeriesDTO() {

    }

    @Override
    public String getType() {
        return "series";
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getSeasonsCount() {
        return seasonsCount;
    }

    public void setSeasonsCount(Integer seasonsCount) {
        this.seasonsCount = seasonsCount;
    }

    public Integer getEpisodesCount() {
        return episodesCount;
    }

    public void setEpisodesCount(Integer episodesCount) {
        this.episodesCount = episodesCount;
    }
}
