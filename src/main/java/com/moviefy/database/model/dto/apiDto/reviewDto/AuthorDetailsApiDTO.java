package com.moviefy.database.model.dto.apiDto.reviewDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AuthorDetailsApiDTO {
    @JsonProperty("avatar_path")
    private String avatarPath;

    private Integer rating;

    public String getAvatarPath() {
        return avatarPath;
    }

    public void setAvatarPath(String avatarPath) {
        this.avatarPath = avatarPath;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }
}
