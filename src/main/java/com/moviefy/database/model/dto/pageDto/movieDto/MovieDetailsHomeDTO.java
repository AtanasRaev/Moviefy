package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.pageDto.CrewHomePageDTO;
import com.moviefy.database.model.dto.pageDto.ProductionHomePageDTO;

import java.time.LocalDate;
import java.util.List;

public class MovieDetailsHomeDTO {
    private long id;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    private String title;

    private String overview;

    private List<CrewHomePageDTO> crew;

    @JsonProperty("production_companies")
    private List<ProductionHomePageDTO> productionCompany;

    private int runtime;

    private String trailer;

    @JsonProperty("vote_average")
    private double voteAverage;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}
