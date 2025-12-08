package com.moviefy.service.ingest.tvSeries;

import com.moviefy.database.model.dto.apiDto.*;
import com.moviefy.database.model.dto.databaseDto.SeasonDTO;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.credit.cast.Cast;
import com.moviefy.database.model.entity.credit.cast.CastTvSeries;
import com.moviefy.database.model.entity.credit.crew.Crew;
import com.moviefy.database.model.entity.credit.crew.CrewTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.EpisodeTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.SeasonTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.credit.cast.CastTvSeriesRepository;
import com.moviefy.database.repository.credit.crew.CrewTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.SeasonTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.api.TmdbCommonEndpointService;
import com.moviefy.service.api.tvSeries.TmdbTvEndpointService;
import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.credit.crew.CrewService;
import com.moviefy.service.productionCompanies.ProductionCompanyService;
import com.moviefy.utils.mappers.TvSeriesMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesIngestService {
    private final TvSeriesRepository tvSeriesRepository;
    private final CrewTvSeriesRepository crewTvSeriesRepository;
    private final CastTvSeriesRepository castTvSeriesRepository;
    private final SeasonTvSeriesRepository seasonTvSeriesRepository;
    private final TmdbTvEndpointService tmdbTvEndpointService;
    private final TmdbCommonEndpointService tmdbCommonEndpointService;
    private final ProductionCompanyService productionCompanyService;
    private final CastService castService;
    private final CrewService crewService;
    private final TvSeriesMapper tvSeriesMapper;
    private final ModelMapper modelMapper;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesIngestService.class);
    private static final int MAX_SERIES_PER_YEAR = 600;

    public TvSeriesIngestService(TvSeriesRepository tvSeriesRepository,
                                 CrewTvSeriesRepository crewTvSeriesRepository,
                                 CastTvSeriesRepository castTvSeriesRepository,
                                 SeasonTvSeriesRepository seasonTvSeriesRepository,
                                 TmdbTvEndpointService tmdbTvEndpointService,
                                 TmdbCommonEndpointService tmdbCommonEndpointService,
                                 ProductionCompanyService productionCompanyService,
                                 CastService castService,
                                 CrewService crewService,
                                 TvSeriesMapper tvSeriesMapper,
                                 ModelMapper modelMapper) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.crewTvSeriesRepository = crewTvSeriesRepository;
        this.castTvSeriesRepository = castTvSeriesRepository;
        this.seasonTvSeriesRepository = seasonTvSeriesRepository;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
        this.tmdbCommonEndpointService = tmdbCommonEndpointService;
        this.productionCompanyService = productionCompanyService;
        this.castService = castService;
        this.crewService = crewService;
        this.tvSeriesMapper = tvSeriesMapper;
        this.modelMapper = modelMapper;
    }

    @Transactional
    public boolean persistSeriesIfEligible(TvSeriesApiDTO dto) {

        LocalDate fad = dto.getFirstAirDate();
        if (fad == null) {
            logger.debug(YELLOW + "Skip series {} — missing firstAirDate" + RESET, dto.getName());
            return false;
        }

        if (this.tvSeriesRepository.findByApiId(dto.getId()).isPresent()) {
            logger.debug(YELLOW + "Skip series {} — already exists" + RESET, dto.getName());
            return false;
        }

        final int rankingYear = fad.getYear();

        TvSeriesApiByIdResponseDTO responseById = this.tmdbTvEndpointService.getTvSeriesResponseById(dto.getId());
        if (responseById == null || responseById.getType() == null) {
            logger.debug(YELLOW + "Skip series {} — missing details or type" + RESET, dto.getName());
            return false;
        }

        if (!responseById.getType().equalsIgnoreCase("scripted")
                && !responseById.getType().equalsIgnoreCase("reality")
                && !responseById.getType().equalsIgnoreCase("documentary")
                && !responseById.getType().equalsIgnoreCase("miniseries")
                && !responseById.getType().equalsIgnoreCase("animation")) {

            logger.warn(YELLOW + "Invalid TV series type: id-{} type-{}" + RESET,
                    dto.getId(), responseById.getType());
            return false;
        }

        long countByYear = this.tvSeriesRepository.findCountByRankingYear(rankingYear);
        if (countByYear >= MAX_SERIES_PER_YEAR) {

            Optional<TvSeries> worstOpt = this.tvSeriesRepository.findLowestRatedSeriesByRankingYear(rankingYear);
            if (worstOpt.isEmpty()) {
                logger.warn(YELLOW + "Skip series {} — year {} full, and no worst series found" + RESET,
                        dto.getName(), rankingYear);
                return false;
            }

            TvSeries worst = worstOpt.get();
            if (!isBetter(dto, worst)) {
                logger.debug(YELLOW + "Skip series {} — not better than worst existing series {}" + RESET,
                        dto.getName(), worst.getName());
                return false;
            }

            logger.info(BLUE + "Replacing worst series '{}' with '{}'" + RESET,
                    worst.getName(), dto.getName());

            detachAndDelete(worst);
        }

        TrailerResponseApiDTO responseTrailer = this.tmdbCommonEndpointService.getTrailerResponseById(dto.getId(), "tv");

        TvSeries tvSeries = this.tvSeriesMapper.mapToTvSeries(dto, responseById, responseTrailer);

        Map<String, Set<ProductionCompany>> productionCompaniesMap =
                this.productionCompanyService.getProductionCompaniesFromResponse(responseById, tvSeries);

        tvSeries.setProductionCompanies(productionCompaniesMap.get("all"));
        if (!productionCompaniesMap.get("toSave").isEmpty()) {
            this.productionCompanyService.saveAllProduction(productionCompaniesMap.get("toSave"));
        }

        tvSeries.setSeasons(mapSeasonsAndEpisodesFromResponse(responseById.getSeasons(), tvSeries));
        this.tvSeriesRepository.save(tvSeries);

        List<CrewApiDTO> crewDto = responseById.getCrew().stream().limit(6).toList();
        Set<Crew> crewSet = this.crewService.mapToSet(crewDto);
        processTvSeriesCrew(crewDto, tvSeries, crewSet);

        Set<CastApiDTO> cast = responseById.getCredits().getCast();
        if (cast != null && !cast.isEmpty()) {
            List<CastApiDTO> castDto = this.castService.filterCastApiDto(cast);
            Set<Cast> castSet = this.castService.mapToSet(castDto);
            processTvSeriesCast(castDto, tvSeries, castSet);
        }

        return true;
    }

    private static boolean isBetter(TvSeriesApiDTO cand, TvSeries worst) {
        int voteCmp = Integer.compare(safeInt(cand.getVoteCount()), safeInt(worst.getVoteCount()));
        if (voteCmp != 0) {
            return voteCmp > 0;
        }
        int popCmp = Double.compare(safeDouble(cand.getPopularity()), safeDouble(worst.getPopularity()));
        if (popCmp != 0) {
            return popCmp > 0;
        }
        return cand.getId() < worst.getApiId();
    }

    private static int safeInt(Integer x) {
        return x == null ? 0 : x;
    }

    private static double safeDouble(Double x) {
        return x == null ? 0.0 : x;
    }

    @Transactional
    protected void detachAndDelete(TvSeries tv) {
        this.castTvSeriesRepository.deleteByTvSeriesId(tv.getId());
        this.crewTvSeriesRepository.deleteByTvSeriesId(tv.getId());
        if (tv.getGenres() != null) {
            tv.getGenres().clear();
        }
        if (tv.getProductionCompanies() != null) {
            tv.getProductionCompanies().clear();
        }

        if (tv.getSeasons() != null) {
            tv.getSeasons().forEach(season -> season.getEpisodes().clear());
            tv.getSeasons().clear();
        }

        this.tvSeriesRepository.delete(tv);
    }

    private Set<SeasonTvSeries> mapSeasonsAndEpisodesFromResponse(List<SeasonDTO> seasonsDTO, TvSeries tvSeries) {
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

    private void processTvSeriesCrew(List<CrewApiDTO> crewDto, TvSeries tvSeries, Set<Crew> crewSet) {
        this.crewService.processCrew(
                crewDto,
                tvSeries,
                c -> crewTvSeriesRepository.findByTvSeriesIdAndCrewApiIdAndJobJob(tvSeries.getId(), c.getId(), "Creator"),
                (c, t) -> {
                    CrewTvSeries crewTvSeries = new CrewTvSeries();
                    crewTvSeries.setTvSeries(t);
                    return crewTvSeries;
                },
                crewTvSeriesRepository::save,
                c -> "Creator",
                crewSet
        );
    }

    private void processTvSeriesCast(List<CastApiDTO> castDto, TvSeries tvSeries, Set<Cast> castSet) {
        this.castService.processCast(
                castDto,
                tvSeries,
                c -> castTvSeriesRepository.findByTvSeriesIdAndCastApiIdAndCharacter(tvSeries.getId(), c.getId(), c.getCharacter()),
                (c, t) -> castService.createCastEntity(
                        c,
                        t,
                        castSet,
                        CastTvSeries::new,
                        CastTvSeries::setTvSeries,
                        CastTvSeries::setCast,
                        CastTvSeries::setCharacter
                ),
                castTvSeriesRepository::save
        );
    }
}
