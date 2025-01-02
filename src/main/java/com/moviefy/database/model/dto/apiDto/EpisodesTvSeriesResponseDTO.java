package com.moviefy.database.model.dto.apiDto;

import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;

import java.util.List;

public class EpisodesTvSeriesResponseDTO {
    List<EpisodeDTO> episodes;

    public List<EpisodeDTO> getEpisodes() {
        return episodes;
    }

    public void setEpisodes(List<EpisodeDTO> episodes) {
        this.episodes = episodes;
    }
}
