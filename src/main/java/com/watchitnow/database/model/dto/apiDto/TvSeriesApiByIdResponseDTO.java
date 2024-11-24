package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

public class TvSeriesApiByIdResponseDTO {
    @JsonProperty("production_companies")
    private List<ProductionApiDTO> productionCompanies;

    @JsonProperty("episode_run_time")
    private List<Integer> episodeRuntime;

    @JsonProperty("created_by")
    private Set<CrewApiDTO> crew;

    private String status;

    public List<ProductionApiDTO> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(List<ProductionApiDTO> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public List<Integer> getEpisodeRuntime() {
        return episodeRuntime;
    }

    public void setEpisodeRuntime(List<Integer> episodeRuntime) {
        this.episodeRuntime = episodeRuntime;
    }

    public Set<CrewApiDTO> getCrew() {
        return crew;
    }

    public void setCrew(Set<CrewApiDTO> crew) {
        this.crew = crew;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
