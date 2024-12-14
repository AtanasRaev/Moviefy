package com.moviefy.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public class MovieApiByIdResponseDTO {
    @JsonProperty("production_companies")
    private List<ProductionApiDTO> productionCompanies;

    @JsonProperty("belongs_to_collection")
    private CollectionApiDTO collection;

    @NotNull
    @Positive
    private Integer runtime;

    public List<ProductionApiDTO> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(List<ProductionApiDTO> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public CollectionApiDTO getCollection() {
        return collection;
    }

    public void setCollection(CollectionApiDTO collection) {
        this.collection = collection;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }
}