package com.moviefy.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public abstract class MediaApiByIdResponseDTO {
    @JsonProperty("production_companies")
    private List<ProductionApiDTO> productionCompanies;

    private MediaResponseCreditsDTO credits;

    public List<ProductionApiDTO> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(List<ProductionApiDTO> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public MediaResponseCreditsDTO getCredits() {
        return credits;
    }

    public void setCredits(MediaResponseCreditsDTO credits) {
        this.credits = credits;
    }
}
