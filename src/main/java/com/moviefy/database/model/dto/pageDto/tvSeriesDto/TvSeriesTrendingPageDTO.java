package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.pageDto.GenrePageDTO;

import java.time.LocalDate;
import java.util.List;

public class TvSeriesTrendingPageDTO extends TvSeriesDTO{
    private String overview;

    private String trailer;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    private LocalDate firstAirDate;

    @JsonProperty("genres")
    private List<GenrePageDTO> allGenres;

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }

    public LocalDate getFirstAirDate() {
        return firstAirDate;
    }

    public void setFirstAirDate(LocalDate firstAirDate) {
        this.firstAirDate = firstAirDate;
    }

    public List<GenrePageDTO> getAllGenres() {
        return allGenres;
    }

    public void setAllGenres(List<GenrePageDTO> allGenres) {
        this.allGenres = allGenres;
    }
}
