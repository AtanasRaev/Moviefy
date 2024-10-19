package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MovieApiByIdResponseDTO {

    @JsonProperty("production_companies")
    private List<ProductionApiDTO> productionCompanies;

    private Integer runtime;

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
}