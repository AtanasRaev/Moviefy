package com.moviefy.database.model.dto.pageDto.mediaDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.pageDto.mediaDto.MediaPageDTO;

public abstract class MovieDTO extends MediaPageDTO {
    private String title;

    @JsonProperty("poster_path")
    private String posterPath;

    private Integer runtime;

    public MovieDTO(Long id, Double voteAverage, Double popularity, Long apiId, String title, Integer runtime, String posterPath) {
        super(id, voteAverage, popularity, apiId);
        this.posterPath = posterPath;
        this.title = title;
        this.runtime = runtime;
    }

    public MovieDTO() {
    }

    @Override
    @JsonProperty("media_type")
    public String getMediaType() {
        return "movie";
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }
}
