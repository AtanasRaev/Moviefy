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

import java.util.*;
import java.util.function.Function;
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
                SeasonTvSeries season = mapSeason(seasonDTO, tvSeries);
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

    @Override
    public boolean updateSeasonsAndEpisodes(List<SeasonDTO> seasonsDTO, TvSeries tvSeries) {
        if (tvSeries == null || seasonsDTO == null || seasonsDTO.isEmpty()) {
            return false;
        }

        boolean updated = false;

        for (SeasonDTO seasonDTO : seasonsDTO) {
            if (seasonDTO.getAirDate() == null || seasonDTO.getSeasonNumber() < 1) {
                continue;
            }

            SeasonTvSeries season = seasonTvSeriesRepository.findByApiId(seasonDTO.getId()).orElse(null);

            if (season == null) {
                season = mapSeason(seasonDTO, tvSeries);
                seasonTvSeriesRepository.save(season);
                updated = true;
                continue;
            }

            if ((season.getPosterPath() == null || season.getPosterPath().isBlank())
                    && seasonDTO.getPosterPath() != null) {
                season.setPosterPath(seasonDTO.getPosterPath());
                updated = true;
            }

            if (!Objects.equals(season.getEpisodeCount(), seasonDTO.getEpisodeCount())) {
                season.setEpisodeCount(seasonDTO.getEpisodeCount());
                updated = true;
            }

            EpisodesTvSeriesResponseDTO resp = tmdbTvEndpointService.getEpisodesResponse(tvSeries.getApiId(), season.getSeasonNumber());

            if (resp == null || resp.getEpisodes() == null) {
                continue;
            }

            Map<Integer, EpisodeTvSeries> byNumber = season.getEpisodes().stream()
                    .collect(Collectors.toMap(EpisodeTvSeries::getEpisodeNumber, Function.identity(), (a, b) -> a));

            for (EpisodeDTO e : resp.getEpisodes()) {
                if (e.getEpisodeNumber() == null || e.getEpisodeNumber() < 1) {
                    continue;
                }

                EpisodeTvSeries target = byNumber.get(e.getEpisodeNumber());

                if (target == null) {
                    target = modelMapper.map(e, EpisodeTvSeries.class);
                    target.setSeason(season);
                    season.getEpisodes().add(target);
                    updated = true;
                    continue;
                }

                boolean beforeUpdateStillPathNull = target.getStillPath() == null;

                String beforeJson = target.toString();
                modelMapper.map(e, target);

                if (beforeUpdateStillPathNull && e.getStillPath() != null) {
                    target.setStillPath(e.getStillPath());
                    updated = true;
                }

                if (!beforeJson.equals(target.toString())) {
                    updated = true;
                }
            }
            seasonTvSeriesRepository.save(season);
        }
        return updated;
    }

    private SeasonTvSeries mapSeason(SeasonDTO seasonDTO, TvSeries tvSeries) {
        SeasonTvSeries season = new SeasonTvSeries();
        season.setApiId(seasonDTO.getId());
        season.setSeasonNumber(seasonDTO.getSeasonNumber());
        season.setAirDate(seasonDTO.getAirDate());
        season.setTvSeries(tvSeries);
        season.setPosterPath(seasonDTO.getPosterPath());
        season.setEpisodeCount(seasonDTO.getEpisodeCount());
        season.setEpisodes(mapEpisodesFromResponse(tvSeries.getApiId(), season));

        return season;
    }

    private Set<EpisodeTvSeries> mapEpisodesFromResponse(long tvSeriesId, SeasonTvSeries season) {
        EpisodesTvSeriesResponseDTO episodesResponse = this.tmdbTvEndpointService.getEpisodesResponse(tvSeriesId, season.getSeasonNumber());

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
