package com.moviefy.service.productionCompanies;

import com.moviefy.database.model.dto.apiDto.mediaDto.MediaApiByIdResponseDTO;
import com.moviefy.database.model.dto.databaseDto.ProductionCompanyDTO;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.media.Media;

import java.util.Map;
import java.util.Set;

public interface ProductionCompanyService {
    Map<String, Set<ProductionCompany>> getProductionCompaniesFromResponse(MediaApiByIdResponseDTO responseById, Media type);

    void saveAllProduction(Set<ProductionCompany> productionCompanies);

    Set<ProductionCompanyDTO> mapProductionCompanies(Media media) ;
}
