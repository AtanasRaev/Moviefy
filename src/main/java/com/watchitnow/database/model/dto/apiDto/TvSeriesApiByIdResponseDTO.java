package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Set;

public class TvSeriesApiByIdResponseDTO {
    @JsonProperty("original_name")
    private String originalName;

    //TODO: Remove the comment!!
//    @JsonProperty("production_companies")
    private List<ProductionApiDTO> productionCompanies;

    //TODO: Remove the comment!!
//    @JsonProperty("episode_run_time")
    private List<Integer> episodeRuntime;

    //TODO: Maybe remove it later!!
    private Double popularity;

    //TODO: Remove the field!!
    private String status;

    //TODO: Maybe remove the field!!
    @JsonProperty("created_by")
    private Set<CrewApiApiDTO> crew;

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

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Set<CrewApiApiDTO> getCrew() {
        return crew;
    }

    public void setCrew(Set<CrewApiApiDTO> crew) {
        this.crew = crew;
    }
}
