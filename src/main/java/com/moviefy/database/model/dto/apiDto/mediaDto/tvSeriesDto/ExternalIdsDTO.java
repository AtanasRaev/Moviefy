package com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ExternalIdsDTO {
    @JsonProperty("imdb_id")
    private String imdbId;

    public String getImdbId() {
        return imdbId;
    }

    public void setImdbId(String imdbId) {
        this.imdbId = imdbId;
    }
}
