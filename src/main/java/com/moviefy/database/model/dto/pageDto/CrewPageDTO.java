package com.moviefy.database.model.dto.pageDto;

public class CrewPageDTO extends CreditPageDTO {
    private String job;

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }
}
