package com.moviefy.database.model.dto.pageDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "name", "poster_path", "seasons", "episodes", "vote_average"})
public class TvSeriesPageDTO extends TvSeriesDTO {
    @JsonProperty("poster_path")
    private String posterPath;

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
