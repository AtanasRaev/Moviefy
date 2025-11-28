package com.moviefy.database.model.entity.credit.crew;

import com.moviefy.database.model.entity.credit.CreditMovie;
import jakarta.persistence.*;

@Entity
@Table(name = "crew_movies")
public class CrewMovie extends CreditMovie {
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "job_id", referencedColumnName = "id")
    private JobCrew job;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "crew_id", referencedColumnName = "id")
    private Crew crew;

    public JobCrew getJob() {
        return job;
    }

    public void setJob(JobCrew job) {
        this.job = job;
    }

    public Crew getCrew() {
        return crew;
    }

    public void setCrew(Crew crew) {
        this.crew = crew;
    }

    public Long getCrewId() {
        return this.crew.getId();
    }
}
