package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class TvSeriesApiByIdResponseDTO {
    @JsonProperty("production_companies")
    private List<ProductionApiDTO> productionCompanies;

    @JsonProperty("episode_run_time")
    private List<Integer> episodeRuntime;

    @JsonProperty("vote_average")
    private Double voteAverage;

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

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }
}
