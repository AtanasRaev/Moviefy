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
import org.springframework.transaction.annotation.Transactional;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private final static Logger logger = LoggerFactory.getLogger(SeasonsServiceImpl.class);

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

    @Transactional
    @Override
    public boolean updateSeasonsAndEpisodes(List<SeasonDTO> seasonsDTO, TvSeries tvSeries) {
        if (tvSeries == null || seasonsDTO == null || seasonsDTO.isEmpty()) {
            return false;
        }

        boolean anyUpdated = false;

        for (SeasonDTO seasonDTO : seasonsDTO) {
            if (seasonDTO.getAirDate() == null
                    || seasonDTO.getSeasonNumber() == null
                    || seasonDTO.getSeasonNumber() < 1) {
                continue;
            }

            Integer seasonNumber = seasonDTO.getSeasonNumber();
            String tvName = tvSeries.getName();

            SeasonTvSeries season = seasonTvSeriesRepository.findByApiId(seasonDTO.getId()).orElse(null);

            if (season == null) {
                SeasonTvSeries created = mapSeason(seasonDTO, tvSeries);
                seasonTvSeriesRepository.save(created);

                logger.info(
                        "TV='{}' S{} → NEW season created",
                        tvName, seasonNumber
                );

                anyUpdated = true;
                continue;
            }

            boolean seasonChanged = false;

            if ((season.getPosterPath() == null || season.getPosterPath().isBlank())
                    && seasonDTO.getPosterPath() != null && !seasonDTO.getPosterPath().isBlank()) {

                season.setPosterPath(seasonDTO.getPosterPath());
                seasonChanged = true;
            }

            boolean hasMissingStills = season.getEpisodes().stream()
                    .anyMatch(ep -> ep.getStillPath() == null || ep.getStillPath().isBlank());

            Integer beforeCount = season.getEpisodeCount();
            Integer afterCount  = seasonDTO.getEpisodeCount();
            boolean countSame = Objects.equals(beforeCount, afterCount);

            if (!countSame) {
                season.setEpisodeCount(afterCount);
                seasonChanged = true;
            }

            if (!hasMissingStills && !seasonChanged) {
                continue;
            }

            if (!hasMissingStills) {
                seasonTvSeriesRepository.save(season);
                anyUpdated = true;
                continue;
            }

            EpisodesTvSeriesResponseDTO resp =
                    tmdbTvEndpointService.getEpisodesResponse(tvSeries.getApiId(), seasonNumber);

            if (resp == null || resp.getEpisodes() == null) {
                if (seasonChanged) {
                    seasonTvSeriesRepository.save(season);
                    anyUpdated = true;
                }
                continue;
            }

            Map<Integer, EpisodeTvSeries> byNumber = season.getEpisodes().stream()
                    .filter(ep -> ep.getEpisodeNumber() != null)
                    .collect(Collectors.toMap(
                            EpisodeTvSeries::getEpisodeNumber,
                            Function.identity(),
                            (a, b) -> a
                    ));

            boolean episodesChanged = false;

            for (EpisodeDTO e : resp.getEpisodes()) {
                Integer epNum = e.getEpisodeNumber();
                if (epNum == null || epNum < 1) continue;

                EpisodeTvSeries target = byNumber.get(epNum);

                if (target == null) {
                    EpisodeTvSeries created = modelMapper.map(e, EpisodeTvSeries.class);
                    created.setSeason(season);
                    season.getEpisodes().add(created);
                    byNumber.put(epNum, created);
                    episodesChanged = true;

                    logger.info(
                            "TV='{}' S{}E{} → NEW episode added",
                            tvName, seasonNumber, epNum
                    );
                    continue;
                }

                boolean episodeChanged = false;

                if (!Objects.equals(target.getName(), e.getName())) {
                    String oldName = target.getName();
                    target.setName(e.getName());
                    episodeChanged = true;
                }

                if ((target.getStillPath() == null || target.getStillPath().isBlank())
                        && e.getStillPath() != null && !e.getStillPath().isBlank()) {
                    target.setStillPath(e.getStillPath());
                    episodeChanged = true;
                }

                if (episodeChanged) {
                    episodesChanged = true;
                }
            }

            if (seasonChanged || episodesChanged) {
                seasonTvSeriesRepository.save(season);
                anyUpdated = true;
            }
        }

        return anyUpdated;
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
