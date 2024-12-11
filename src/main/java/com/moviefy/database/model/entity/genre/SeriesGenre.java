package com.moviefy.database.model.entity.genre;

import com.moviefy.database.model.entity.media.TvSeries;
import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "series_genres")
public class SeriesGenre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column
    private Long apiId;

    @ManyToMany(mappedBy = "genres", fetch = FetchType.LAZY)
    private List<TvSeries> tvSeries;

    public SeriesGenre() {}

    public SeriesGenre(String name, Long apiId) {
        this.name = name;
        this.apiId = apiId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getApiId() {
        return apiId;
    }

    public void setApiId(Long apiId) {
        this.apiId = apiId;
    }

    public List<TvSeries> getTvSeries() {
        return tvSeries;
    }

    public void setTvSeries(List<TvSeries> tvSeries) {
        this.tvSeries = tvSeries;
    }
}
