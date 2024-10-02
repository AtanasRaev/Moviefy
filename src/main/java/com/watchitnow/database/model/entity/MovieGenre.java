package com.watchitnow.database.model.entity;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "movies_genres")
public class MovieGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String name;

    @Column(name = "api_id")
    private Long apiId;

    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    private List<Movie> movies;

    public MovieGenre() {}

    public MovieGenre(String name, Long apiId) {
        this.name = name;
        this.apiId = apiId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }
}
