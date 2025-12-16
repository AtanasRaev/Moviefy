package com.moviefy.database.model.entity.media.tvSeries;

import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.genre.SeriesGenre;
import com.moviefy.database.model.entity.media.Media;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.HashSet;
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
            cascade = {CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REMOVE},
            orphanRemoval = true)
    private Set<SeasonTvSeries> seasons = new HashSet<>();

    @Column(name = "number_of_seasons")
    private Integer numberOfSeasons;

    @Column(name = "number_of_episodes")
    private Integer numberOfEpisodes;

    @Column(name = "status")
    private String status;

    @Column
    private String type;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String statusTvSeries) {
        this.status = statusTvSeries;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Set<SeasonTvSeries> getSeasons() {
        return seasons;
    }

    public void setSeasons(Set<SeasonTvSeries> seasons) {
        this.seasons = seasons;
    }

    public Integer getNumberOfSeasons() {
        return numberOfSeasons;
    }

    public void setNumberOfSeasons(Integer numberOfSeasons) {
        this.numberOfSeasons = numberOfSeasons;
    }

    public Integer getNumberOfEpisodes() {
        return numberOfEpisodes;
    }

    public void setNumberOfEpisodes(Integer numberOfEpisodes) {
        this.numberOfEpisodes = numberOfEpisodes;
    }

    public Set<ProductionCompany> getProductionCompanies() {
        return productionCompanies;
    }

    public void setProductionCompanies(Set<ProductionCompany> productionCompanies) {
        this.productionCompanies = productionCompanies;
    }
}
