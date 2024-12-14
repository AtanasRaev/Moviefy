package com.moviefy.database.model.dto.apiDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class CollectionApiDTO {
    private long id;

    private String name;

    @JsonProperty("poster_path")
    private String posterPath;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }
}
