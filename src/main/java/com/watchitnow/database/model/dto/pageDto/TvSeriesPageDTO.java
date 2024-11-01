package com.watchitnow.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "name", "poster_path", "genres"})
public class TvSeriesPageDTO extends MediaPageDTO{
    private String name;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
