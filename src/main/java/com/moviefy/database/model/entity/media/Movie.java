package com.moviefy.database.model.entity.media;

import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.genre.MovieGenre;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "movies")
public class Movie extends Media {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<MovieGenre> genres;

    @Column
    private String title;

    @Column(name = "original_title")
    private String originalTitle;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "collection_id", referencedColumnName = "id")
    private Collection collection;

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

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
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

    public Collection getCollection() {
        return collection;
    }

    public void setCollection(Collection collection) {
        this.collection = collection;
    }
}
