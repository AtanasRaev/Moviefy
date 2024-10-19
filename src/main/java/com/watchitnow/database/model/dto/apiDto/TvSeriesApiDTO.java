package com.watchitnow.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

public class TvSeriesApiDTO extends MediaApiDTO{
    private String name;

    @JsonProperty("first_air_date")
    private LocalDate firstAirDate;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LocalDate getFirstAirDate() {
        return firstAirDate;
    }

    public void setFirstAirDate(LocalDate firstAirDate) {
        this.firstAirDate = firstAirDate;
    }
}
