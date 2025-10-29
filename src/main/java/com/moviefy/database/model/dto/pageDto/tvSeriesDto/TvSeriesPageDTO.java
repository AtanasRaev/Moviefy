package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "name", "poster_path", "seasons", "episodes", "vote_average"})
public class TvSeriesPageDTO extends TvSeriesDTO {
    @JsonProperty("poster_path")
    private String posterPath;

    private Integer year;

    public TvSeriesPageDTO(Long id, Double voteAverage, Double popularity, String name, Integer seasonsCount, Integer episodesCount, String posterPath, Integer year) {
        super(id, voteAverage, popularity, name, seasonsCount, episodesCount);
        this.posterPath = posterPath;
        this.year = year;
    }

    public TvSeriesPageDTO() {
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }
}
