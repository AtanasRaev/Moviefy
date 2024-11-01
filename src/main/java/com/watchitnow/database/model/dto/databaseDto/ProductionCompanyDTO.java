package com.watchitnow.database.model.dto.databaseDto;


import com.fasterxml.jackson.annotation.JsonProperty;

public class ProductionCompanyDTO {
    @JsonProperty("logo_path")
    private String logoPath;

    private String name;

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
