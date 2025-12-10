package com.moviefy.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.databaseDto.SeasonDTO;

import java.util.List;
import java.util.Set;

public class TvSeriesApiByIdResponseDTO extends MediaApiByIdResponseDTO{
    @JsonProperty("created_by")
    private Set<CrewApiDTO> crew;

    private String type;

    private String status;

    List<SeasonDTO> seasons;

    @JsonProperty("number_of_seasons")
    private Integer numberOfSeasons;

    @JsonProperty("number_of_episodes")
    private Integer numberOfEpisodes;

    @JsonProperty("external_ids")
    private ExternalIdsDTO externalIds;

    public Set<CrewApiDTO> getCrew() {
        return crew;
    }

    public void setCrew(Set<CrewApiDTO> crew) {
        this.crew = crew;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public List<SeasonDTO> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<SeasonDTO> seasons) {
        this.seasons = seasons;
    }

    public ExternalIdsDTO getExternalIds() {
        return externalIds;
    }

    public void setExternalIds(ExternalIdsDTO externalIds) {
        this.externalIds = externalIds;
    }

    public Integer getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public void setNumberOfSeasons(Integer numberOfSeasons) {
        this.numberOfSeasons = numberOfSeasons;
    }

    public Integer getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(Integer numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
    }
}
