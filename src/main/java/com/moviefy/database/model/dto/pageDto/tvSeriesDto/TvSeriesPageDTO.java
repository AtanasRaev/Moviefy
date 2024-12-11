package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.moviefy.database.model.dto.pageDto.MediaPageDTO;

@JsonPropertyOrder({"id", "name", "poster_path", "first_air_date", "seasons", "episode_time", "genres"})
public class TvSeriesPageDTO extends MediaPageDTO {
    private String name;

    @JsonProperty("seasons")
    private Integer seasonsCount;

    @JsonProperty("episodes_count")
    private Integer episodesCount;

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
