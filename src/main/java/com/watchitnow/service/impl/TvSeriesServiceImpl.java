package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.*;
import com.watchitnow.database.model.dto.databaseDto.SeasonDTO;
import com.watchitnow.database.model.dto.detailsDto.SeasonTvSeriesDTO;
import com.watchitnow.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.watchitnow.database.model.dto.pageDto.TvSeriesPageDTO;
import com.watchitnow.database.model.entity.ProductionCompany;
import com.watchitnow.database.model.entity.credit.Cast.Cast;
import com.watchitnow.database.model.entity.credit.Cast.CastTvSeries;
import com.watchitnow.database.model.entity.credit.Crew.Crew;
import com.watchitnow.database.model.entity.credit.Crew.CrewTvSeries;
import com.watchitnow.database.model.entity.media.SeasonTvSeries;
import com.watchitnow.database.model.entity.media.TvSeries;
import com.watchitnow.database.repository.CastTvSeriesRepository;
import com.watchitnow.database.repository.CrewTvSeriesRepository;
import com.watchitnow.database.repository.SeasonTvSeriesRepository;
import com.watchitnow.database.repository.TvSeriesRepository;
import com.watchitnow.service.*;
import com.watchitnow.utils.*;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TvSeriesServiceImpl implements TvSeriesService {
    private final TvSeriesRepository tvSeriesRepository;
    private final CrewTvSeriesRepository crewTvSeriesRepository;
    private final CastTvSeriesRepository castTvSeriesRepository;
    private final SeriesGenreService seriesGenreService;
    private final CastService castService;
    private final CrewService crewService;
    private final SeasonTvSeriesRepository seasonTvSeriesRepository;
    private final ProductionCompanyService productionCompanyService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final TrailerMappingUtil trailerMappingUtil;
    private final ModelMapper modelMapper;
    private final MediaRetrievalUtil mediaRetrievalUtil;
    private final TvSeriesMapper tvSeriesMapper;
    private static final Logger logger = LoggerFactory.getLogger(TvSeriesServiceImpl.class);

    public TvSeriesServiceImpl(TvSeriesRepository tvSeriesRepository,
                               CrewTvSeriesRepository crewTvSeriesRepository,
                               CastTvSeriesRepository castTvSeriesRepository,
                               SeriesGenreService seriesGenreService,
                               CastService castService,
                               CrewService crewService,
                               SeasonTvSeriesRepository seasonTvSeriesRepository,
                               ProductionCompanyService productionCompanyService,
                               ApiConfig apiConfig,
                               RestClient restClient,
                               TrailerMappingUtil trailerMappingUtil,
                               ModelMapper modelMapper,
                               MediaRetrievalUtil mediaRetrievalUtil,
                               TvSeriesMapper tvSeriesMapper) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.crewTvSeriesRepository = crewTvSeriesRepository;
        this.castTvSeriesRepository = castTvSeriesRepository;
        this.seriesGenreService = seriesGenreService;
        this.castService = castService;
        this.crewService = crewService;
        this.seasonTvSeriesRepository = seasonTvSeriesRepository;
        this.productionCompanyService = productionCompanyService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.trailerMappingUtil = trailerMappingUtil;
        this.modelMapper = modelMapper;
        this.mediaRetrievalUtil = mediaRetrievalUtil;
        this.tvSeriesMapper = tvSeriesMapper;
    }

    @Override
    public Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable) {
        return mediaRetrievalUtil.fetchContentFromDateRange(
                pageable,
                dateRange -> tvSeriesRepository.findByFirstAirDateBetweenWithGenres(dateRange.start(), dateRange.end()),
                tvSeries -> modelMapper.map(tvSeries, TvSeriesPageDTO.class)
        );
    }

    @Override
    public TvSeriesDetailsDTO getTvSeriesById(long id) {
        TvSeriesDetailsDTO tv = this.modelMapper.map(this.tvSeriesRepository.findTvSeriesById(id), TvSeriesDetailsDTO.class);
        if (tv == null) {
            return null;
        }
        tv.setSeasons(tv.getSeasons().stream()
                .sorted(Comparator.comparingInt(SeasonTvSeriesDTO::getSeasonNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        return tv;
    }

    @Override
    public Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType) {
        return this.tvSeriesRepository.findByGenreName(genreType)
                .stream()
                .map(tvSeries -> modelMapper.map(tvSeries, TvSeriesPageDTO.class))
                .collect(Collectors.toSet());
    }


    //     @Scheduled(fixedDelay = 100000000)
    //TODO
    private void updateTvSeries() {
    }

//    @Scheduled(fixedDelay = 500)
    private void fetchSeries() {
        logger.info("Starting to fetch tv series...");

        int year = LocalDate.now().getYear();
        int page = 1;
        int totalPages;
        int savedSeriesCount = 0;

        LocalDate startDate = LocalDate.of(year, 12, 1);

        if (!isEmpty()) {
            List<TvSeries> oldestTvSeries = this.tvSeriesRepository.findOldestTvSeries();
            if (!oldestTvSeries.isEmpty()) {
                TvSeries oldestTV = oldestTvSeries.get(0);

                year = oldestTV.getFirstAirDate().getYear();
                startDate = LocalDate.of(year, oldestTV.getFirstAirDate().getMonthValue(), oldestTV.getFirstAirDate().getDayOfMonth());

                long tvSeriesByYearAndMonth = this.tvSeriesRepository.countTvSeriesInDateRange(oldestTV.getFirstAirDate().getYear(), oldestTV.getFirstAirDate().getMonthValue());

                if (tvSeriesByYearAndMonth > 20) {
                    page = (int) ((tvSeriesByYearAndMonth / 20) + 1);
                }
            }
        }

        LocalDate endDate = LocalDate.of(year, startDate.getMonthValue(), startDate.lengthOfMonth());

        if (year == 1999) {
            return;
        }

        for (int i = 0; i < 40; i++) {
            logger.info("Fetching page {} of date range {} to {}", page, startDate, endDate);

            TvSeriesResponseApiDTO response = getTvSeriesResponse(page, startDate, endDate);
            totalPages = response.getTotalPages();

            for (TvSeriesApiDTO dto : response.getResults()) {

                if ((dto.getPosterPath() == null || dto.getPosterPath().isBlank()) || (dto.getOverview() == null || dto.getOverview().isBlank())) {
                    continue;
                }

                if (this.tvSeriesRepository.findByApiId(dto.getId()).isEmpty()) {
                    TvSeriesApiByIdResponseDTO responseById = getTvSeriesResponseById(dto.getId());
                    if (responseById == null) {
                        continue;
                    }

                    SeasonTvSeriesResponseApiDTO seasonsResponse = getSeasonsResponse(dto.getId());

                    TrailerResponseApiDTO responseTrailer = this.trailerMappingUtil.getTrailerResponseById(dto.getId(),
                            this.apiConfig.getUrl(),
                            this.apiConfig.getKey(),
                            "tv");

                    TvSeries tvSeries = tvSeriesMapper.mapToTvSeries(dto, responseById, responseTrailer);

                    Map<String, Set<ProductionCompany>> productionCompaniesMap = productionCompanyService.getProductionCompaniesFromResponse(responseById, tvSeries);
                    tvSeries.setProductionCompanies(productionCompaniesMap.get("all"));

                    if (!productionCompaniesMap.get("toSave").isEmpty()) {
                        this.productionCompanyService.saveAllProductionCompanies(productionCompaniesMap.get("toSave"));
                    }

                    Set<SeasonTvSeries> seasons = mapSeasonsFromResponse(seasonsResponse, tvSeries);
                    tvSeries.setSeasons(seasons);

                    this.tvSeriesRepository.save(tvSeries);
                    this.seasonTvSeriesRepository.saveAll(seasons);
                    savedSeriesCount++;
                    logger.info("Saved tv series: {}", tvSeries.getName());

                    List<CrewApiDTO> crewDto = responseById.getCrew().stream().limit(6).toList();
                    Set<Crew> crewSet = this.crewService.mapToSet(crewDto);

                    processTvSeriesCrew(crewDto, tvSeries, crewSet);

                    MediaResponseCreditsDTO creditsById = getCreditsById(tvSeries.getApiId());
                    if (creditsById == null) {
                        this.tvSeriesRepository.save(tvSeries);
                        continue;
                    }

                    List<CastApiApiDTO> castDto = this.castService.filterCastApiDto(creditsById);
                    Set<Cast> castSet = this.castService.mapToSet(castDto);

                    processTvSeriesCast(castDto, tvSeries, castSet);
                }
            }

            DateRange result = DatePaginationUtil.updatePageAndDate(page, totalPages, i, savedSeriesCount, startDate, endDate, year);
            page = result.getPage();
            startDate = result.getStartDate();
            endDate = result.getEndDate();
            year = result.getYear();
        }

        logger.info("Finished fetching tvSeries.");
    }

    private Set<SeasonTvSeries> mapSeasonsFromResponse(SeasonTvSeriesResponseApiDTO seasonsResponse, TvSeries tvSeries) {
        Set<SeasonTvSeries> seasons = new HashSet<>();

        for (SeasonDTO seasonDTO : seasonsResponse.getSeasons()) {
            if (this.seasonTvSeriesRepository.findByApiId(seasonDTO.getId()).isEmpty()) {
                SeasonTvSeries season = new SeasonTvSeries();
                season.setApiId(seasonDTO.getId());
                season.setSeasonNumber(seasonDTO.getSeasonNumber());
                season.setAirDate(seasonDTO.getAirDate());
                season.setEpisodeCount(seasonDTO.getEpisodeCount());
                season.setTvSeries(tvSeries);
                seasons.add(season);
            }
        }
        return seasons;
    }

    private boolean isEmpty() {
        return this.tvSeriesRepository.count() == 0;
    }

    private void processTvSeriesCast(List<CastApiApiDTO> castDto, TvSeries tvSeries, Set<Cast> castSet) {
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


    private TvSeriesResponseApiDTO getTvSeriesResponse(int page, LocalDate startDate, LocalDate endDate) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/discover/tv?page=%d&first_air_date.gte=%s&first_air_date.lte=%s&api_key="
                        + this.apiConfig.getKey()
                , page, startDate, endDate);
        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(TvSeriesResponseApiDTO.class);
    }

    private SeasonTvSeriesResponseApiDTO getSeasonsResponse(long id) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d?api_key=%s", id, this.apiConfig.getKey());

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(SeasonTvSeriesResponseApiDTO.class);
    }

    private TvSeriesApiByIdResponseDTO getTvSeriesResponseById(Long apiId) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d?api_key=" + this.apiConfig.getKey(), apiId);
        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(TvSeriesApiByIdResponseDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching tv-series with ID: " + apiId + " - " + e.getMessage());
            return null;
        }
    }

    private MediaResponseCreditsDTO getCreditsById(Long apiId) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d/credits?api_key=%s", apiId, this.apiConfig.getKey());

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(MediaResponseCreditsDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching credits with ID: " + apiId + " - " + e.getMessage());
            return null;
        }
    }
}
