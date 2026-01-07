package com.moviefy.database.model.dto.pageDto.user;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageWithGenreProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.tvSeriesDto.TvSeriesPageWithGenreProjection;

import java.time.LocalDateTime;
import java.util.List;

public class UserProfileDTO {
    private String email;

    @JsonProperty("first_name")
    private String firstName;

    @JsonProperty("last_name")
    private String lastName;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("favorite_movies")
    private List<MoviePageWithGenreProjection> favoriteMovies;

    @JsonProperty("favorite_tv_series")
    private List<TvSeriesPageWithGenreProjection> favoriteTvSeries;

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public List<MoviePageWithGenreProjection> getFavoriteMovies() {
        return favoriteMovies;
    }

    public void setFavoriteMovies(List<MoviePageWithGenreProjection> favoriteMovies) {
        this.favoriteMovies = favoriteMovies;
    }

    public List<TvSeriesPageWithGenreProjection> getFavoriteTvSeries() {
        return favoriteTvSeries;
    }

    public void setFavoriteTvSeries(List<TvSeriesPageWithGenreProjection> favoriteTvSeries) {
        this.favoriteTvSeries = favoriteTvSeries;
    }
}
