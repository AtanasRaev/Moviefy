package com.watchitnow.database.model.entity;

import java.time.LocalDate;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tv_series")
public class TvSeries extends Media {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "series_genre",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<SeriesGenre> genres;

    @Column
    private String name;

    @Column
    private String originalName;

    @Column(name = "first_air_date")
    private LocalDate firstAirDate;

    @OneToMany(mappedBy = "tvSeries",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    private Set<SeasonTvSeries> seasons;

    @Column(name = "episode_run_time")
    private Integer episodeRunTime;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tv_series_production",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "production_id")
    )
    private Set<ProductionCompany> productionCompanies;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Set<SeriesGenre> getGenres() {
        return genres;
    }

    public void setGenres(Set<SeriesGenre> genres) {
        this.genres = genres;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public LocalDate getFirstAirDate() {
        return firstAirDate;
    }

    public void setFirstAirDate(LocalDate firstAirDate) {
        this.firstAirDate = firstAirDate;
    }

    public Integer getEpisodeRunTime() {
        return episodeRunTime;
    }

    public void setEpisodeRunTime(Integer episodeRunTime) {
        this.episodeRunTime = episodeRunTime;
    }

    public Set<SeasonTvSeries> getSeasons() {
        return seasons;
    }

    public void setSeasons(Set<SeasonTvSeries> seasons) {
        this.seasons = seasons;
    }

    public Set<ProductionCompany> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(Set<ProductionCompany> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }
}
