package com.moviefy.database.model.dto.databaseDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class EpisodeDTO {
    @JsonProperty("episode_number")
    private Integer episodeNumber;

    private String name;

    @JsonProperty("still_path")
    private String stillPath;

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getStillPath() {
        return stillPath;
    }

    public void setStillPath(String stillPath) {
        this.stillPath = stillPath;
    }
}
