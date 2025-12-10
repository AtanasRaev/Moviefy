package com.moviefy.database.model.dto.apiDto.reviewDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ReviewResponseApiDTO {
    private Integer page;

    @JsonProperty("total_pages")
    private Integer totalPages;

    @JsonProperty("total_results")
    private Integer totalResults;

    private List<ReviewApiDTO> results;

    public Integer getPage() {
        return page;
    }

    public void setPage(Integer page) {
        this.page = page;
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

    public List<ReviewApiDTO> getResults() {
        return results;
    }

    public void setResults(List<ReviewApiDTO> results) {
        this.results = results;
    }
}
