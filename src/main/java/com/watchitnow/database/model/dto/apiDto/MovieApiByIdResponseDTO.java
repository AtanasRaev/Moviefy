package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MovieApiByIdResponseDTO {
    //TODO: Remove the field!!
    @JsonProperty("original_title")
    private String originalTitle;

    //TODO: Remove the comment!!
//    @JsonProperty("production_companies")
    private List<ProductionApiDTO> productionCompanies;

    //TODO: Remove jsonIgnore!!
    @JsonIgnore
    private Integer runtime;

    //TODO: Maybe remove it later!!
    private Double popularity;

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public List<ProductionApiDTO> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(List<ProductionApiDTO> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }
}