package com.moviefy.database.model.entity.media;

import jakarta.persistence.*;

@Entity
@Table(name = "status_tv")
public class StatusTvSeries {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String status;

    public StatusTvSeries(String status) {
        this.status = status;
    }

    public StatusTvSeries() {

    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
