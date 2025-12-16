package com.moviefy.database.model.entity.media.tvSeries;

import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(
        name = "episodes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"season_id", "episode_number"})
)
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EpisodeTvSeries that)) return false;

        if (episodeNumber == null || that.episodeNumber == null) return false;
        if (season == null || that.season == null) return false;

        Long thisSeasonApiId = season.getApiId();
        Long thatSeasonApiId = that.season.getApiId();

        if (thisSeasonApiId == null || thatSeasonApiId == null) return false;

        return episodeNumber.equals(that.episodeNumber)
                && thisSeasonApiId.equals(thatSeasonApiId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(
                (season != null ? season.getApiId() : null),
                episodeNumber
        );
    }

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
