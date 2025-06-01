package com.moviefy.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

@JsonPropertyOrder({"id", "type", "title", "poster_path", "vote_average", "year", "genre", "trailer"})
public class SearchResultDTO {
    private Long id;
    
    private String type;
    
    private String title;
    
    @JsonProperty("poster_path")
    private String posterPath;
    
    @JsonProperty("vote_average")
    private Double voteAverage;
    
    private Integer year;
    
    private String genre;
    
    private String trailer;
    
    private Integer runtime;
    
    @JsonProperty("seasons")
    private Integer seasonsCount;
    
    @JsonProperty("episodes")
    private Integer episodesCount;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    public String getTrailer() {
        return trailer;
    }

    public void setTrailer(String trailer) {
        this.trailer = trailer;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public Integer getSeasonsCount() {
        return seasonsCount;
    }

    public void setSeasonsCount(Integer seasonsCount) {
        this.seasonsCount = seasonsCount;
    }

    public Integer getEpisodesCount() {
        return episodesCount;
    }

    public void setEpisodesCount(Integer episodesCount) {
        this.episodesCount = episodesCount;
    }
}