package com.moviefy.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.pageDto.CrewHomePageDTO;
import com.moviefy.database.model.dto.pageDto.ProductionHomePageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieDTO;

import java.time.LocalDate;
import java.util.List;

public class MovieDetailsHomeDTO extends MovieDTO {
    @JsonProperty("backdrop_path")
    private String backdropPath;

    private String overview;

    private List<CrewHomePageDTO> crew;

    @JsonProperty("production_companies")
    private List<ProductionHomePageDTO> productionCompany;

    private String trailer;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public List<CrewHomePageDTO> getCrew() {
        return crew;
    }

    public void setCrew(List<CrewHomePageDTO> crew) {
        this.crew = crew;
    }

    public List<ProductionHomePageDTO> getProductionCompany() {
        return productionCompany;
    }

    public void setProductionCompany(List<ProductionHomePageDTO> productionCompany) {
        this.productionCompany = productionCompany;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }


    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}
