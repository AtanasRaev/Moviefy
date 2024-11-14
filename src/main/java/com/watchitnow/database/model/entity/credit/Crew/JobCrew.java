package com.watchitnow.database.model.entity.credit.Crew;

import jakarta.persistence.*;

@Entity
@Table(name = "job_crew")
public class JobCrew {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column
    private String job;

    public JobCrew(String job) {
        this.job = job;
    }

    public JobCrew() {

    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}
