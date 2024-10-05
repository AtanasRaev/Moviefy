package com.watchitnow.database.model.entity;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "movies")
public class Movie {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "movie_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private List<MovieGenre> genres;

    @Column(name = "api_id", unique = true)
    private Long apiId;

    @Column
    private String title;

    @Column(columnDefinition = "TEXT")
    private String overview;

    @Column
    private Double popularity;

    @Column(name = "poster_path")
    private String posterPath;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    @Column(name = "vote_average")
    private Double voteAverage;

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

    public List<MovieGenre> getGenres() {
        return genres;
    }

    public void setGenres(List<MovieGenre> genres) {
        this.genres = genres;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public Double getVoteAverage() {
        return voteAverage;
    }

    public void setVoteAverage(Double voteAverage) {
        this.voteAverage = voteAverage;
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
