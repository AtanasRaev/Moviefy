package com.moviefy.service.ingest.tvSeries;

import com.moviefy.database.model.dto.apiDto.CastApiDTO;
import com.moviefy.database.model.dto.apiDto.TrailerResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesApiDTO;
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
import com.moviefy.utils.EntityComparator;
import com.moviefy.utils.mappers.TvSeriesMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesIngestService {
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

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesIngestService.class);
    private static final int MAX_SERIES_PER_YEAR = 600;

    public TvSeriesIngestService(TvSeriesRepository tvSeriesRepository,
                                 CrewTvSeriesRepository crewTvSeriesRepository,
                                 CastTvSeriesRepository castTvSeriesRepository,
                                 TmdbTvEndpointService tmdbTvEndpointService,
                                 TmdbCommonEndpointService tmdbCommonEndpointService,
                                 ProductionCompanyService productionCompanyService,
                                 CastService castService,
                                 CrewService crewService,
                                 TvSeriesMapper tvSeriesMapper,
                                 SeasonsService seasonsService) {
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
            if (!EntityComparator.isBetter(dto, worst)) {
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

        tvSeries.setSeasons(this.seasonsService.mapSeasonsAndEpisodesFromResponse(responseById.getSeasons(), tvSeries));
        this.tvSeriesRepository.save(tvSeries);

        this.crewService.processTvSeriesCrew(responseById.getCrew(), tvSeries);

        Set<CastApiDTO> cast = responseById.getCredits().getCast();
        if (cast != null && !cast.isEmpty()) {
            this.castService.processTvSeriesCast(cast, tvSeries);
        }

        return true;
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
}
