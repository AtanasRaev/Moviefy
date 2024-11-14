package com.watchitnow.database.model.dto.apiDto;

public class CrewApiApiDTO extends CreditApiDTO {
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
