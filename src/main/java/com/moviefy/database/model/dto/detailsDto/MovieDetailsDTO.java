package com.moviefy.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.moviefy.database.model.dto.pageDto.movieDto.MovieCollectionPageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageDTO;

import java.time.LocalDate;
import java.util.List;

@JsonPropertyOrder({
        "id",
        "title",
        "original_title",
        "poster_path",
        "backdrop_path",
        "release_date",
        "overview",
        "trailer",
        "runtime",
        "vote_average",
        "api_id",
        "genres",
        "production_companies",
        "cast",
        "crew"
})
public class MovieDetailsDTO extends MediaDetailsDTO {
    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private Integer runtime;

    private List<MovieCollectionPageDTO> collection;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
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

    public List<MovieCollectionPageDTO> getCollection() {
        return collection;
    }

    public void setCollection(List<MovieCollectionPageDTO> collection) {
        this.collection = collection;
    }
}
