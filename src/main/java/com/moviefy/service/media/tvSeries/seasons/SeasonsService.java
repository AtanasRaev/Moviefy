package com.moviefy.service.media.tvSeries.seasons;

import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.databaseDto.SeasonDTO;
import com.moviefy.database.model.entity.media.tvSeries.SeasonTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;

import java.util.List;
import java.util.Set;

public interface SeasonsService {
    Set<SeasonTvSeries> mapSeasonsAndEpisodesFromResponse(List<SeasonDTO> seasonsDTO, TvSeries tvSeries);

    Set<SeasonTvSeries> findAllByTvSeriesId(Long id);

    List<EpisodeDTO> getEpisodesFromSeason(Long seasonId);

    Integer getSeasonNumberById(Long seasonId);

    boolean updateSeasonsAndEpisodes(List<SeasonDTO> seasonsDTO, TvSeries tvSeries);
}
