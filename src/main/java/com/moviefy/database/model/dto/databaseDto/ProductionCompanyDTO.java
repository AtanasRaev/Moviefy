package com.moviefy.database.model.dto.databaseDto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductionCompanyDTO {
    private long id;

    @JsonProperty("logo_path")
    private String logoPath;

    private String name;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getLogoPath() {
        return logoPath;
    }

    public void setLogoPath(String logoPath) {
        this.logoPath = logoPath;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
