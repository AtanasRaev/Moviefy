package com.moviefy.database.model.dto.detailsDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;

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
        "collection_name",
        "genres",
        "production_companies",
        "cast",
        "crew",
        "collection"
})
public class MovieDetailsDTO extends MediaDetailsDTO {
    private String title;

    @JsonProperty("original_title")
    private String originalTitle;

    @JsonProperty("release_date")
    private LocalDate releaseDate;

    private Integer runtime;

    @JsonProperty("collection_name")
    private String collectionTitle;

    private List<MoviePageWithGenreDTO> collection;

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

    public String getCollectionTitle() {
        return collectionTitle;
    }

    public void setCollectionTitle(String collectionTitle) {
        this.collectionTitle = collectionTitle;
    }

    public List<MoviePageWithGenreDTO> getCollection() {
        return collection;
    }

    public void setCollection(List<MoviePageWithGenreDTO> collection) {
        this.collection = collection;
    }
}
