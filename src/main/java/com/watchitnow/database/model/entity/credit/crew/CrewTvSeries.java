package com.watchitnow.database.model.entity.credit.crew;

import com.watchitnow.database.model.entity.credit.CreditTvSeries;
import jakarta.persistence.*;

@Entity
@Table(name = "crew_tv")
public class CrewTvSeries extends CreditTvSeries {
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
}
