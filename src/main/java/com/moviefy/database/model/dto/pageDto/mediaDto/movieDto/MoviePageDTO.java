package com.moviefy.database.model.dto.pageDto.mediaDto.movieDto;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "title", "poster_path", "runtime", "vote_average"})
public class MoviePageDTO extends MovieDTO {
    private Integer year;

    public MoviePageDTO(Long id, Double voteAverage, Double popularity, Long apiId, String title, Integer runtime, String posterPath, Integer year) {
        super(id, voteAverage, popularity, apiId, title, runtime, posterPath);
        this.year = year;
    }

    public MoviePageDTO() {
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
