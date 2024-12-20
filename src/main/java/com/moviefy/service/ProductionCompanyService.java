package com.moviefy.service;

import com.moviefy.database.model.dto.databaseDto.ProductionCompanyDTO;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.media.Media;

import java.util.Map;
import java.util.Set;

public interface ProductionCompanyService {
    Map<String, Set<ProductionCompany>> getProductionCompaniesFromResponse(Object responseById, Media type);

    void saveAllProduction(Set<ProductionCompany> productionCompanies);

    Set<ProductionCompanyDTO> mapProductionCompanies(Media media) ;
}
