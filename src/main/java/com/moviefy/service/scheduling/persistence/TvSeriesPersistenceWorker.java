package com.moviefy.service.scheduling.persistence;

import com.moviefy.config.FetchMediaConfig;
import com.moviefy.database.model.dto.apiDto.creditDto.CastApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.credit.cast.CastTvSeriesRepository;
import com.moviefy.database.repository.credit.crew.CrewTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.api.TmdbCommonEndpointService;
import com.moviefy.service.api.tvSeries.TmdbTvEndpointService;
import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.credit.crew.CrewService;
import com.moviefy.service.media.tvSeries.seasons.SeasonsService;
import com.moviefy.service.productionCompanies.ProductionCompanyService;
import com.moviefy.service.scheduling.IngestEnum;
import com.moviefy.service.scheduling.MediaEventPublisher;
import com.moviefy.utils.EntityComparator;
import com.moviefy.utils.mappers.TvSeriesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesPersistenceWorker {
    private final TvSeriesRepository tvSeriesRepository;
    private final CrewTvSeriesRepository crewTvSeriesRepository;
    private final CastTvSeriesRepository castTvSeriesRepository;
    private final TmdbTvEndpointService tmdbTvEndpointService;
    private final TmdbCommonEndpointService tmdbCommonEndpointService;
    private final ProductionCompanyService productionCompanyService;
    private final CastService castService;
    private final CrewService crewService;
    private final SeasonsService seasonsService;
    private final TvSeriesMapper tvSeriesMapper;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesPersistenceWorker.class);

    public TvSeriesPersistenceWorker(TvSeriesRepository tvSeriesRepository,
                                     CrewTvSeriesRepository crewTvSeriesRepository,
                                     CastTvSeriesRepository castTvSeriesRepository,
                                     TmdbTvEndpointService tmdbTvEndpointService,
                                     TmdbCommonEndpointService tmdbCommonEndpointService,
                                     ProductionCompanyService productionCompanyService,
                                     CastService castService,
                                     CrewService crewService,
                                     TvSeriesMapper tvSeriesMapper,
                                     SeasonsService seasonsService,
                                     MediaEventPublisher mediaEventPublisher) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.crewTvSeriesRepository = crewTvSeriesRepository;
        this.castTvSeriesRepository = castTvSeriesRepository;
        this.seasonsService = seasonsService;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
        this.tmdbCommonEndpointService = tmdbCommonEndpointService;
        this.productionCompanyService = productionCompanyService;
        this.castService = castService;
        this.crewService = crewService;
        this.tvSeriesMapper = tvSeriesMapper;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public IngestEnum persistSeriesIfEligible(Long apiId) {
        TvSeriesApiByIdResponseDTO responseById = this.tmdbTvEndpointService.getTvSeriesResponseById(apiId);
        if (responseById == null || responseById.getType() == null) {
            logger.debug(YELLOW + "Skip series with apiId{} — missing details or type" + RESET, apiId);
            return IngestEnum.INVALID;
        }

        LocalDate fad = responseById.getFirstAirDate();
        if (fad == null) {
            logger.debug(YELLOW + "Skip series {} — missing firstAirDate" + RESET, responseById.getName());
            return IngestEnum.INVALID;
        }

        if (this.tvSeriesRepository.findByApiId(responseById.getId()).isPresent()) {
            logger.debug(YELLOW + "Skip series {} — already exists" + RESET, responseById.getName());
            return IngestEnum.INVALID;
        }

        final int rankingYear = fad.getYear();

        if (!responseById.getType().equalsIgnoreCase("scripted")
                && !responseById.getType().equalsIgnoreCase("reality")
                && !responseById.getType().equalsIgnoreCase("documentary")
                && !responseById.getType().equalsIgnoreCase("miniseries")
                && !responseById.getType().equalsIgnoreCase("animation")) {

            logger.warn(YELLOW + "Invalid TV series type: id-{} type-{}" + RESET,
                    responseById.getId(), responseById.getType());
            return IngestEnum.INVALID;
        }

        long countByYear = this.tvSeriesRepository.findCountByRankingYear(rankingYear);
        if (countByYear >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
            Optional<TvSeries> worstOpt = this.tvSeriesRepository.findLowestRatedSeriesByRankingYear(rankingYear);
            if (worstOpt.isEmpty()) {
                logger.warn(YELLOW + "Skip series {} — year {} full, and no worst series found" + RESET,
                        responseById.getName(), rankingYear);
                return IngestEnum.STOP_EVALUATION;
            }

            TvSeries worst = worstOpt.get();
            if (!EntityComparator.isBetter(responseById, worst)) {
                logger.debug(YELLOW + "Skip series {} — not better than worst existing series {}" + RESET,
                        responseById.getName(), worst.getName());
                return IngestEnum.STOP_EVALUATION;
            }

            logger.info(BLUE + "Replacing worst series '{}' with '{}'" + RESET,
                    worst.getName(), responseById.getName());

            detachAndDelete(worst);
        }

        TrailerResponseApiDTO responseTrailer = this.tmdbCommonEndpointService.getTrailerResponseById(responseById.getId(), "tv");

        TvSeries tvSeries = this.tvSeriesMapper.mapToTvSeries(responseById, responseTrailer);

        Map<String, Set<ProductionCompany>> productionCompaniesMap =
                this.productionCompanyService.getProductionCompaniesFromResponse(responseById, tvSeries);

        tvSeries.setProductionCompanies(productionCompaniesMap.get("all"));
        if (!productionCompaniesMap.get("toSave").isEmpty()) {
            this.productionCompanyService.saveAllProduction(productionCompaniesMap.get("toSave"));
        }

        tvSeries.setSeasons(this.seasonsService.mapSeasonsAndEpisodesFromResponse(responseById.getSeasons(), tvSeries));
        this.tvSeriesRepository.save(tvSeries);

        this.crewService.processTvSeriesCrew(responseById.getCrew(), tvSeries);

        Set<CastApiDTO> cast = responseById.getCredits().getCast();
        if (cast != null && !cast.isEmpty()) {
            this.castService.processTvSeriesCast(cast, tvSeries);

            Set<Long> castIds = this.castTvSeriesRepository.findCastIdsByTvSeriesId(tvSeries.getId());
            this.mediaEventPublisher.publishCastByTvSeriesChangedEvent(castIds);
            this.mediaEventPublisher.publishCastByMediaChangedEvent(castIds);

            Set<Long> crewIds = this.crewTvSeriesRepository.findCrewIdsByTvSeriesId(tvSeries.getId());
            this.mediaEventPublisher.publishCrewByTvSeriesChangedEvent(crewIds);
            this.mediaEventPublisher.publishCrewByMediaChangedEvent(crewIds);
        }

        return IngestEnum.INSERTED;
    }

    @Transactional
    protected void detachAndDelete(TvSeries tv) {
        Set<Long> castIds = this.castTvSeriesRepository.findCastIdsByTvSeriesId(tv.getId());
        this.castTvSeriesRepository.deleteByTvSeriesId(tv.getId());
        this.mediaEventPublisher.publishCastByTvSeriesChangedEvent(castIds);
        this.mediaEventPublisher.publishCastByMediaChangedEvent(castIds);

        Set<Long> crewIds = this.crewTvSeriesRepository.findCrewIdsByTvSeriesId(tv.getId());
        this.mediaEventPublisher.publishCrewByTvSeriesChangedEvent(crewIds);
        this.mediaEventPublisher.publishCrewByMediaChangedEvent(crewIds);
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
}
