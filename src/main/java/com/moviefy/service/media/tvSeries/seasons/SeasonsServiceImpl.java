package com.moviefy.service.media.tvSeries.seasons;

import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.EpisodesTvSeriesResponseDTO;
import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.databaseDto.SeasonDTO;
import com.moviefy.database.model.entity.media.tvSeries.EpisodeTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.SeasonTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.media.tvSeries.EpisodeTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.SeasonTvSeriesRepository;
import com.moviefy.service.api.tvSeries.TmdbTvEndpointService;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class SeasonsServiceImpl implements SeasonsService {
    private final SeasonTvSeriesRepository seasonTvSeriesRepository;
    private final EpisodeTvSeriesRepository episodeTvSeriesRepository;
    private final TmdbTvEndpointService tmdbTvEndpointService;
    private final ModelMapper modelMapper;

    public SeasonsServiceImpl(SeasonTvSeriesRepository seasonTvSeriesRepository,
                              EpisodeTvSeriesRepository episodeTvSeriesRepository,
                              TmdbTvEndpointService tmdbTvEndpointService,
                              ModelMapper modelMapper) {
        this.seasonTvSeriesRepository = seasonTvSeriesRepository;
        this.episodeTvSeriesRepository = episodeTvSeriesRepository;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
        this.modelMapper = modelMapper;
    }

    @Override
    public Set<SeasonTvSeries> mapSeasonsAndEpisodesFromResponse(List<SeasonDTO> seasonsDTO, TvSeries tvSeries) {
        if ((seasonsDTO == null || seasonsDTO.isEmpty()) || tvSeries == null) {
            return new HashSet<>();
        }

        Set<SeasonTvSeries> seasons = new HashSet<>();

        for (SeasonDTO seasonDTO : seasonsDTO) {
            if (seasonDTO.getAirDate() == null || seasonDTO.getSeasonNumber() < 1) {
                continue;
            }

            if (this.seasonTvSeriesRepository.findByApiId(seasonDTO.getId()).isEmpty()) {
                SeasonTvSeries season = new SeasonTvSeries();
                season.setApiId(seasonDTO.getId());
                season.setSeasonNumber(seasonDTO.getSeasonNumber());
                season.setAirDate(seasonDTO.getAirDate());
                season.setEpisodeCount(seasonDTO.getEpisodeCount());
                season.setPosterPath(seasonDTO.getPosterPath());
                season.setTvSeries(tvSeries);
                season.setEpisodes(mapEpisodesFromResponse(tvSeries.getApiId(), season));
                seasons.add(season);
            }
        }
        return seasons;
    }

    @Override
    public Set<SeasonTvSeries> findAllByTvSeriesId(Long id) {
        return this.seasonTvSeriesRepository.findAllByTvSeriesId(id);
    }

    @Override
    public List<EpisodeDTO> getEpisodesFromSeason(Long seasonId) {
        return this.episodeTvSeriesRepository.findAllBySeasonId(seasonId)
                .stream()
                .map(episode -> this.modelMapper.map(episode, EpisodeDTO.class))
                .sorted(Comparator.comparing(EpisodeDTO::getEpisodeNumber))
                .toList();
    }

    @Override
    public Integer getSeasonNumberById(Long seasonId) {
        return this.seasonTvSeriesRepository.findById(seasonId)
                .map(SeasonTvSeries::getSeasonNumber)
                .orElse(null);
    }

    private Set<EpisodeTvSeries> mapEpisodesFromResponse(long id, SeasonTvSeries season) {
        EpisodesTvSeriesResponseDTO episodesResponse = this.tmdbTvEndpointService.getEpisodesResponse(id, season.getSeasonNumber());

        if (episodesResponse == null) {
            return new HashSet<>();
        }

        return episodesResponse.getEpisodes()
                .stream()
                .map(dto -> {
                    EpisodeTvSeries map = this.modelMapper.map(dto, EpisodeTvSeries.class);
                    map.setSeason(season);
                    return map;
                })
                .collect(Collectors.toSet());
    }
}
