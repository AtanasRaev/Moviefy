package com.moviefy.database.model.entity.credit;

import com.moviefy.database.model.entity.media.TvSeries;
import jakarta.persistence.*;

@MappedSuperclass
public abstract class CreditTvSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tv_series_id", referencedColumnName = "id")
    private TvSeries tvSeries;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public TvSeries getTvSeries() {
        return tvSeries;
    }

    public void setTvSeries(TvSeries tvSeries) {
        this.tvSeries = tvSeries;
    }
}
