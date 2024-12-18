package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.moviefy.database.model.dto.pageDto.MediaPageDTO;

@JsonPropertyOrder({"id", "title", "poster_path", "runtime", "vote_average"})
public class MoviePageDTO extends MovieDTO {
    @JsonProperty("poster_path")
    private String posterPath;

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
