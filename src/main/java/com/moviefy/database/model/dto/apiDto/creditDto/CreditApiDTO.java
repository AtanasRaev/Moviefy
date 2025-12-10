package com.moviefy.database.model.dto.apiDto.creditDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public abstract class CreditApiDTO {
    private long id;

    private String name;

    @JsonProperty("profile_path")
    private String profilePath;

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

    public String getProfilePath() {
        return profilePath;
    }

    public void setProfilePath(String profilePath) {
        this.profilePath = profilePath;
    }
}
