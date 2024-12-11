package com.moviefy.database.model.entity.media;

import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.genre.SeriesGenre;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "tv_series")
public class TvSeries extends Media {
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "series_genre",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id")
    )
    private Set<SeriesGenre> genres;

    @Column
    private String name;

    @Column(name = "original_name")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "status_id", referencedColumnName = "id")
    private StatusTvSeries statusTvSeries;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "tv_series_production",
            joinColumns = @JoinColumn(name = "series_id"),
            inverseJoinColumns = @JoinColumn(name = "production_id")
    )
    private Set<ProductionCompany> productionCompanies;

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

    public StatusTvSeries getStatusTvSeries() {
        return statusTvSeries;
    }

    public void setStatusTvSeries(StatusTvSeries statusTvSeries) {
        this.statusTvSeries = statusTvSeries;
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
