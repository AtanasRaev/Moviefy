package com.watchitnow.databse.model.dto;

import java.util.List;

public class SeasonTvSeriesResponseApiDTO {
    List<SeasonDTO> seasons;

    public List<SeasonDTO> getSeasons() {
        return seasons;
    }

    public void setSeasons(List<SeasonDTO> seasons) {
        this.seasons = seasons;
    }
}
