package com.watchitnow.database.model.entity;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "production_companies")
public class ProductionCompany {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    @Column(name = "logo_path")
    private String logoPath;

    @Column
    private String name;

    @Column(unique = true)
    private Long apiId;

    @ManyToMany(mappedBy = "productionCompanies", fetch = FetchType.LAZY)
    private List<Movie> movies;

    @ManyToMany(mappedBy = "productionCompanies", fetch = FetchType.LAZY)
    private List<TvSeries> tvSeries;

    public ProductionCompany() {
        this.movies = new ArrayList<>();
        this.tvSeries = new ArrayList<>();
    }

    public Long getId() {
        return Id;
    }

    public void setId(Long id) {
        Id = id;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Movie> getMovies() {
        return movies;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public void setMovies(List<Movie> movies) {
        this.movies = movies;
    }

    public List<TvSeries> getTvSeries() {
        return tvSeries;
    }

    public void setTvSeries(List<TvSeries> tvSeries) {
        this.tvSeries = tvSeries;
    }
}
