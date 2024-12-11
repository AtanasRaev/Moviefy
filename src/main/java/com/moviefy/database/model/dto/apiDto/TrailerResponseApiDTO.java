package com.moviefy.database.model.dto.apiDto;

import java.util.List;

public class TrailerResponseApiDTO {
    private List<TrailerApiDTO> results;

    public List<TrailerApiDTO> getResults() {
        return results;
    }

    public void setResults(List<TrailerApiDTO> results) {
        this.results = results;
    }
}
