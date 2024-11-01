package com.watchitnow.database.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "movies")
public class Movie extends Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<MovieGenre> genres;

    @Column
    private String title;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column
    private Integer runtime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_production",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "production_id")
    )
    Set<ProductionCompany> productionCompanies;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public Set<MovieGenre> getGenres() {
        return genres;
    }

    public void setGenres(Set<MovieGenre> genres) {
        this.genres = genres;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }


    public Set<ProductionCompany> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(Set<ProductionCompany> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }
}
