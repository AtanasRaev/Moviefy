package com.moviefy.database.model.dto.apiDto;

public class CrewApiDTO extends CreditApiDTO {
    private String job;

    private Double popularity;

    public String getJob() {
        return job;
    }

    public void setJob(String job) {
        this.job = job;
    }

    public Double getPopularity() {
        return popularity;
    }

    public void setPopularity(Double popularity) {
        this.popularity = popularity;
    }
}
