package com.moviefy.database.model.dto.apiDto.mediaDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class MovieResponseApiDTO {
    private Integer page;

    private List<MovieApiDTO> results;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;

    public MovieResponseApiDTO() {
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
    }

    public List<MovieApiDTO> getResults() {
        return results;
    }

    public void setResults(List<MovieApiDTO> results) {
        this.results = results;
    }

    public Integer getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(Integer totalPages) {
        this.totalPages = totalPages;
    }

    public Integer getTotalResults() {
        return totalResults;
    }

    public void setTotalResults(Integer totalResults) {
        this.totalResults = totalResults;
    }
}
