package com.watchitnow.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.watchitnow.database.model.dto.databaseDto.ProductionCompanyDTO;
import com.watchitnow.database.model.dto.pageDto.GenrePageDTO;

import java.util.Set;

public abstract class MediaDetailsDTO {
    @JsonProperty("api_id")
    private Long apiId;

    private String overview;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("poster_path")
    private String posterPath;

    @JsonProperty("vote_average")
    private Double voteAverage;

    private String trailer;

    private Set<GenrePageDTO> genres;

    @JsonProperty("production_companies")
    private Set<ProductionCompanyDTO> productionCompanies;

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public Set<GenrePageDTO> getGenres() {
        return genres;
    }

    public Set<ProductionCompanyDTO> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(Set<ProductionCompanyDTO> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }

    public void setGenres(Set<GenrePageDTO> genres) {
        this.genres = genres;
    }
}
