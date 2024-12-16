package com.moviefy.database.model.dto.pageDto.movieDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class MovieHomeDTO {
    private long id;

    private String title;

    private int runtime;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getRuntime() {
        return runtime;
    }

    public void setRuntime(int runtime) {
        this.runtime = runtime;
    }

    public String getBackdropPath() {
        return backdropPath;
    }

    public void setBackdropPath(String backdropPath) {
        this.backdropPath = backdropPath;
    }
}
