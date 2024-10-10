package com.watchitnow.service.impl;

import com.watchitnow.database.model.dto.apiDto.MovieApiByIdResponseDTO;
import com.watchitnow.database.model.dto.apiDto.ProductionApiDTO;
import com.watchitnow.database.model.dto.apiDto.TvSeriesApiByIdResponseDTO;
import com.watchitnow.database.model.entity.Movie;
import com.watchitnow.database.model.entity.ProductionCompany;
import com.watchitnow.database.model.entity.TvSeries;
import com.watchitnow.database.repository.ProductionCompanyRepository;
import com.watchitnow.service.ProductionCompanyService;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ProductionCompanyServiceImpl implements ProductionCompanyService {
    private final ProductionCompanyRepository productionCompanyRepository;

    public ProductionCompanyServiceImpl(ProductionCompanyRepository productionCompanyRepository) {
        this.productionCompanyRepository = productionCompanyRepository;
    }

    @Override
    public Map<String, Set<ProductionCompany>> getProductionCompaniesFromResponse(Object responseById, Object type) {
        Map<String, Set<ProductionCompany>> productionCompaniesMap = new HashMap<>();
        productionCompaniesMap.put("all", new HashSet<>());
        productionCompaniesMap.put("toSave", new HashSet<>());

        List<ProductionApiDTO> productionCompanies = new ArrayList<>();

        if (getType(type).equals("movie")) {
            productionCompanies = ((MovieApiByIdResponseDTO) responseById).getProductionCompanies();
        } else if (getType(type).equals("tv-series")) {
            productionCompanies = ((TvSeriesApiByIdResponseDTO) responseById).getProductionCompanies();
        }

        for (ProductionApiDTO company : productionCompanies) {
            List<Long> list = productionCompaniesMap.get("all").stream()
                    .map(ProductionCompany::getApiId)
                    .toList();

            if (list.contains(company.getId())) {
                continue;
            }

            Optional<ProductionCompany> byApiId = this.productionCompanyRepository.findByApiId(company.getId());
            if (byApiId.isEmpty()) {
                ProductionCompany productionCompany = new ProductionCompany();

                productionCompany.setApiId(company.getId());
                productionCompany.setName(company.getName());
                productionCompany.setLogoPath(company.getLogoPath());

                String typeStr = getType(type);

                if (typeStr.equals("movie")) {
                    productionCompany.getMovies().add((Movie) type);
                } else if (typeStr.equals("tv-series")) {
                    productionCompany.getTvSeries().add((TvSeries) type);
                }

                productionCompaniesMap.get("all").add(productionCompany);
                productionCompaniesMap.get("toSave").add(productionCompany);
            } else {
                productionCompaniesMap.get("all").add(byApiId.get());
            }
        }

        return productionCompaniesMap;
    }

    @Override
    public void saveAllProductionCompanies(Set<ProductionCompany> productionCompanies) {
        this.productionCompanyRepository.saveAll(productionCompanies);
    }

    private String getType(Object type) {

        if (type instanceof Movie) {
            return "movie";
        } else if (type instanceof TvSeries) {
            return "tv-series";
        }

        throw new IllegalArgumentException("Unknown content type");
    }
}
