package com.watchitnow.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;
import java.util.Set;

@JsonPropertyOrder({ "name", "poster_path", "backdrop_path", "first_air_date", "overview", "trailer", "episode_run_time", "vote_average", "api_id", "genres", "production_companies", "seasons"})
public class TvSeriesDetailsDTO extends MediaDetailsDTO {
    private String name;

    @JsonProperty("first_air_date")
    private LocalDate firstAirDate;

    private Set<SeasonTvSeriesDTO> seasons;

    @JsonProperty("episode_run_time")
    private Integer episodeRunTime;


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public Integer getEpisodeRunTime() {
        return episodeRunTime;
    }

    public void setEpisodeRunTime(Integer episodeRunTime) {
        this.episodeRunTime = episodeRunTime;
    }
}
