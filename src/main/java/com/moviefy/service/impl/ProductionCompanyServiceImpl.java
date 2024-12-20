package com.moviefy.service.impl;

import com.moviefy.database.model.dto.apiDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.ProductionApiDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.dto.databaseDto.ProductionCompanyDTO;
import com.moviefy.database.model.dto.pageDto.ProductionHomePageDTO;
import com.moviefy.database.model.entity.media.Media;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.media.TvSeries;
import com.moviefy.database.repository.ProductionCompanyRepository;
import com.moviefy.service.ProductionCompanyService;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ProductionCompanyServiceImpl implements ProductionCompanyService {
    private final ProductionCompanyRepository productionCompanyRepository;
    private final ModelMapper modelMapper;

    public ProductionCompanyServiceImpl(ProductionCompanyRepository productionCompanyRepository,
                                        ModelMapper modelMapper) {
        this.productionCompanyRepository = productionCompanyRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public Map<String, Set<ProductionCompany>> getProductionCompaniesFromResponse(Object responseById, Media type) {
        Map<String, Set<ProductionCompany>> productionCompaniesMap = new HashMap<>();
        productionCompaniesMap.put("all", new HashSet<>());
        productionCompaniesMap.put("toSave", new HashSet<>());

        List<ProductionApiDTO> productionCompanies;

        if (type instanceof Movie) {
            productionCompanies = ((MovieApiByIdResponseDTO) responseById).getProductionCompanies();
        } else if (type instanceof TvSeries) {
            productionCompanies = ((TvSeriesApiByIdResponseDTO) responseById).getProductionCompanies();
        } else {
            throw new IllegalArgumentException("Unknown content type");
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

                if (type instanceof Movie) {
                    productionCompany.getMovies().add((Movie) type);
                } else {
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
    public void saveAllProduction(Set<ProductionCompany> productionCompanies) {
        this.productionCompanyRepository.saveAll(productionCompanies);
    }

    @Override
    public Set<ProductionCompanyDTO> mapProductionCompanies(Media media) {
        if (media instanceof Movie movie) {
            return mapProductionCompaniesForMedia(movie.getProductionCompanies());
        } else if (media instanceof TvSeries tvSeries) {
            return mapProductionCompaniesForMedia(tvSeries.getProductionCompanies());
        } else {
            return Collections.emptySet();
        }
    }

    private Set<ProductionCompanyDTO> mapProductionCompaniesForMedia(Set<ProductionCompany> productionCompanies) {
        return productionCompanies.stream()
                .sorted(Comparator.comparing(ProductionCompany::getId))
                .map(production -> this.modelMapper.map(production, ProductionCompanyDTO.class))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
