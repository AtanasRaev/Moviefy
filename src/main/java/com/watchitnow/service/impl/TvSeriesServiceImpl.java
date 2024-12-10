package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.*;
import com.watchitnow.database.model.dto.databaseDto.SeasonDTO;
import com.watchitnow.database.model.dto.detailsDto.SeasonTvSeriesDTO;
import com.watchitnow.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.watchitnow.database.model.dto.pageDto.TvSeriesPageDTO;
import com.watchitnow.database.model.entity.ProductionCompany;
import com.watchitnow.database.model.entity.credit.cast.Cast;
import com.watchitnow.database.model.entity.credit.cast.CastTvSeries;
import com.watchitnow.database.model.entity.credit.crew.Crew;
import com.watchitnow.database.model.entity.credit.crew.CrewTvSeries;
import com.watchitnow.database.model.entity.media.SeasonTvSeries;
import com.watchitnow.database.model.entity.media.TvSeries;
import com.watchitnow.database.repository.CastTvSeriesRepository;
import com.watchitnow.database.repository.CrewTvSeriesRepository;
import com.watchitnow.database.repository.SeasonTvSeriesRepository;
import com.watchitnow.database.repository.TvSeriesRepository;
import com.watchitnow.service.*;
import com.watchitnow.utils.MediaRetrievalUtil;
import com.watchitnow.utils.TrailerMappingUtil;
import com.watchitnow.utils.TvSeriesMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    public Page<TvSeriesPageDTO> getTvSeriesFromCurrentMonth(Pageable pageable, int totalPages) {
        return mediaRetrievalUtil.fetchContentFromDateRange(
                totalPages,
                pageable,
                dateRange -> tvSeriesRepository.findByFirstAirDateBetweenWithGenres(dateRange.start(), dateRange.end()),
                tvSeries -> {
                    TvSeriesPageDTO map = modelMapper.map(tvSeries, TvSeriesPageDTO.class);

                    Set<SeasonTvSeries> seasons = tvSeries.getSeasons();
                    if (seasons != null && !seasons.isEmpty()) {
                        mapSeasonsToPageDTO(seasons, map);
                    }
                    return map;
                }
        );
    }

    @Override
    public TvSeriesDetailsDTO getTvSeriesDetailsById(long id) {
        TvSeriesDetailsDTO tv = this.modelMapper.map(this.tvSeriesRepository.findTvSeriesById(id), TvSeriesDetailsDTO.class);
        if (tv == null) {
            return null;
        }

        tv.setSeasons(tv.getSeasons().stream()
                .sorted(Comparator.comparingInt(SeasonTvSeriesDTO::getSeasonNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new)));

        tv.setCast(this.castService.getCastByMediaId("tv", id));
        tv.setCrew(this.crewService.getCrewByMediaId("tv", id));
        return tv;
    }

    @Override
    public Set<TvSeriesPageDTO> getTvSeriesByGenre(String genreType) {
        return this.tvSeriesRepository.findByGenreName(genreType)
                .stream()
                .map(tvSeries -> modelMapper.map(tvSeries, TvSeriesPageDTO.class))
                .collect(Collectors.toSet());
    }

    @Override
    public List<TvSeriesPageDTO> getMostPopularTvSeries(int totalItems) {
        return this.tvSeriesRepository.findAllSortedByPopularity(totalItems)
                .stream()
                .map(tvSeries -> {
                    TvSeriesPageDTO map = this.modelMapper.map(tvSeries, TvSeriesPageDTO.class);
                    mapSeasonsToPageDTO(new HashSet<>(this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId())), map);
                    return map;
                })
                .toList();
    }


    //     @Scheduled(fixedDelay = 100000000)
    //TODO
    private void updateTvSeries() {
    }

    //    @Scheduled(fixedDelay = 500000)
    private void fetchSeries() {
        logger.info("Starting to fetch tv series...");

        int year = LocalDate.now().getYear();

        int page = 1;
        Long countOldestTvSeries = this.tvSeriesRepository.countOldestTvSeries();
        int count = countOldestTvSeries.intValue();

        if (countOldestTvSeries > 0) {
            year = this.tvSeriesRepository.findOldestTvSeriesYear();

            if (countOldestTvSeries >= 1000) {
                year -= 1;
                count = 0;
            } else {
                page = (int) Math.ceil(countOldestTvSeries / 20.0);
            }
        }

        while (count < 1000) {
            logger.info("Fetching page {} of year {}", page, year);

            TvSeriesResponseApiDTO response = getTvSeriesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn("No results returned for page {} of year {}", page, year);
                break;
            }

            if (page > response.getTotalPages()) {
                logger.info("Reached the last page for year {}.", year);
                year -= 1;
                page = 1;
                count = 0;
                continue;
            }

            for (TvSeriesApiDTO dto : response.getResults()) {

                if (count >= 1000) {
                    break;
                }

                if (isInvalid(dto)) {
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
                    count++;
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
            page++;
        }

        logger.info("Finished fetching tvSeries.");
    }

    private static boolean isInvalid(TvSeriesApiDTO dto) {
        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || dto.getName() == null || dto.getName().isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }

    private static void mapSeasonsToPageDTO(Set<SeasonTvSeries> seasons, TvSeriesPageDTO map) {
        seasons.stream()
                .max(Comparator.comparing(SeasonTvSeries::getSeasonNumber))
                .ifPresent(lastSeason -> {
                    map.setSeasonsCount(lastSeason.getSeasonNumber() > 411
                            ? lastSeason.getSeasonNumber()
                            : seasons.size());
                });
    }

    private Set<SeasonTvSeries> mapSeasonsFromResponse(SeasonTvSeriesResponseApiDTO seasonsResponse, TvSeries tvSeries) {
        Set<SeasonTvSeries> seasons = new HashSet<>();

        for (SeasonDTO seasonDTO : seasonsResponse.getSeasons()) {
            if (seasonDTO.getAirDate() == null || seasonDTO.getSeasonNumber() > 0) {
                continue;
            }

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

    private TvSeriesResponseApiDTO getTvSeriesResponseByDateAndVoteCount(int page, int year) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/discover/tv?first_air_date.gte=%d-01-01&first_air_date.lte=%d-12-31&sort_by=vote_count.desc&api_key=%s&page=%d",
                year, year, this.apiConfig.getKey(), page);

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
