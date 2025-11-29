package com.moviefy.database.model.dto.pageDto;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.Instant;

public class ReviewPageDTO {
    private String author;

    @JsonProperty("author_path")
    private String authorPath;

    private Integer rating;

    private String content;

    @JsonProperty("created_at")
    private Instant createdAt;

    @JsonProperty("updated_at")
    private Instant updatedAt;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getAuthorPath() {
        return authorPath;
    }

    public void setAuthorPath(String authorPath) {
        this.authorPath = authorPath;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
