package com.moviefy.database.model.entity.media;

import jakarta.persistence.*;

@Entity
@Table(name = "episodes")
public class EpisodeTvSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String name;

    @Column(name = "still_path")
    private String stillPath;

    @Column(name = "episode_number")
    private Integer episodeNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", referencedColumnName = "id")
    private SeasonTvSeries season;


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

    public String getStillPath() {
        return stillPath;
    }

    public void setStillPath(String stillPath) {
        this.stillPath = stillPath;
    }

    public Integer getEpisodeNumber() {
        return episodeNumber;
    }

    public void setEpisodeNumber(Integer episodeNumber) {
        this.episodeNumber = episodeNumber;
    }

    public SeasonTvSeries getSeason() {
        return season;
    }

    public void setSeason(SeasonTvSeries season) {
        this.season = season;
    }
}
