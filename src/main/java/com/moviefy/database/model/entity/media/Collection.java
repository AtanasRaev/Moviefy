package com.moviefy.database.model.entity.media;

import jakarta.persistence.*;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "collections")
public class Collection {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "api_id", unique = true)
    private long apiId;

    @OneToMany(mappedBy = "collection", fetch = FetchType.EAGER)
    private Set<Movie> movies;

    @Column(name = "vote_count_average")
    private Double voteCountAverage;

    @Column(name = "has_movies")
    private boolean hasMovies;

    public Collection() {
    }

    public Collection(long apiId, String name, String posterPath) {
        this.apiId = apiId;
        this.name = name;
        this.posterPath = posterPath;
        this.movies = new LinkedHashSet<>();
        this.voteCountAverage = 0.0;
        this.hasMovies = false;
    }

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

    public long getApiId() {
        return apiId;
    }

    public void setApiId(long apiId) {
        this.apiId = apiId;
    }

    public Set<Movie> getMovies() {
        return movies;
    }

    public void setMovies(Set<Movie> movies) {
        this.movies = movies;
    }

    public Double getVoteCountAverage() {
        return voteCountAverage;
    }

    public void setVoteCountAverage(Double voteCountAverage) {
        this.voteCountAverage = voteCountAverage;
    }

    public boolean isHasMovies() {
        return hasMovies;
    }

    public void setHasMovies(boolean hasMovies) {
        this.hasMovies = hasMovies;
    }
}
