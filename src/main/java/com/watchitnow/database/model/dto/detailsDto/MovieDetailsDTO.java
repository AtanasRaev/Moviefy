package com.watchitnow.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.time.LocalDate;

@JsonPropertyOrder({ "title", "poster_path", "backdrop_path", "release_date", "overview", "trailer", "runtime", "vote_average", "api_id", "genres", "production_companies"})
public class MovieDetailsDTO extends MediaDetailsDTO {
    private String title;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private Integer runtime;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }
}
