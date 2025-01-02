package com.moviefy.database.model.entity.media;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.util.Set;

@Entity
@Table(name = "seasons")
public class SeasonTvSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "air_date")
    private LocalDate airDate;

    @Column(name = "episode_count")
    private Integer episodeCount;

    @Column(name = "season_number")
    private Integer seasonNumber;

    @Column(name = "poster_path")
    private String posterPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_series_id", referencedColumnName = "id")
    private TvSeries tvSeries;

    @Column(name = "api_id", unique = true)
    private Long apiId;

    @OneToMany(mappedBy = "season",
            fetch = FetchType.LAZY,
            cascade = {CascadeType.PERSIST, CascadeType.REMOVE},
            orphanRemoval = true)
    private Set<EpisodeTvSeries> episodes;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getAirDate() {
        return airDate;
    }

    public void setAirDate(LocalDate airDate) {
        this.airDate = airDate;
    }

    public Integer getEpisodeCount() {
        return episodeCount;
    }

    public void setEpisodeCount(Integer episodeCount) {
        this.episodeCount = episodeCount;
    }

    public Integer getSeasonNumber() {
        return seasonNumber;
    }

    public void setSeasonNumber(Integer seasonNumber) {
        this.seasonNumber = seasonNumber;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public TvSeries getTvSeries() {
        return tvSeries;
    }

    public void setTvSeries(TvSeries tvSeries) {
        this.tvSeries = tvSeries;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public Set<EpisodeTvSeries> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(Set<EpisodeTvSeries> episodes) {
        this.episodes = episodes;
    }
}
