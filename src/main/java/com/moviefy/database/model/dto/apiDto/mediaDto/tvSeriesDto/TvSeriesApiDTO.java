package com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.apiDto.mediaDto.MediaApiDTO;

import java.time.LocalDate;

public class TvSeriesApiDTO extends MediaApiDTO {
    private String name;

    @JsonProperty("original_name")
    private String originalName;

    @JsonProperty("first_air_date")
    private LocalDate firstAirDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public LocalDate getFirstAirDate() {
        return firstAirDate;
    }

    public void setFirstAirDate(LocalDate firstAirDate) {
        this.firstAirDate = firstAirDate;
    }
}
