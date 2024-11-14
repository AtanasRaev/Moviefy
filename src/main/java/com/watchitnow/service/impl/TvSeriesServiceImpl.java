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
import com.watchitnow.database.model.entity.credit.Crew.JobCrew;
import com.watchitnow.database.model.entity.media.SeasonTvSeries;
import com.watchitnow.database.model.entity.media.StatusTvSeries;
import com.watchitnow.database.model.entity.media.TvSeries;
import com.watchitnow.database.repository.*;
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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class TvSeriesServiceImpl implements TvSeriesService {
    private final TvSeriesRepository tvSeriesRepository;
    private final StatusTvSeriesRepository statusTvSeriesRepository;
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
                               StatusTvSeriesRepository statusTvSeriesRepository,
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
        this.statusTvSeriesRepository = statusTvSeriesRepository;
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

    @Override
    public StatusTvSeries findByName(String name) {
        return this.statusTvSeriesRepository.findByStatus(name).orElse(null);
    }

    @Scheduled(fixedDelay = 1000000)
    //TODO
    private void updateTvSeries() {
        for (long i = 1; i <= 109663; i++) {
            Optional<TvSeries> tvSeriesOptional = this.tvSeriesRepository.findById(i);
            if (tvSeriesOptional.isEmpty()) {
                continue;
            }
            TvSeries tvSeries = tvSeriesOptional.get();
            TvSeriesApiByIdResponseDTO dtoTvSeriesById = getTvSeriesResponseById(tvSeries.getApiId());

            if (dtoTvSeriesById == null || tvSeries.getOverview().isBlank()) {
                tvSeries.getGenres().clear();
                tvSeries.getProductionCompanies().clear();
                this.tvSeriesRepository.delete(tvSeries);
                continue;
            }

            BigDecimal popularity = BigDecimal.valueOf(dtoTvSeriesById.getPopularity()).setScale(1, RoundingMode.HALF_UP);
            tvSeries.setPopularity(popularity.doubleValue());
            tvSeries.setOriginalName(!dtoTvSeriesById.getOriginalName().equals(tvSeries.getName()) && !dtoTvSeriesById.getOriginalName().isBlank() ? dtoTvSeriesById.getOriginalName() : null);
            if (!dtoTvSeriesById.getStatus().isEmpty()) {
                StatusTvSeries status = findByName(dtoTvSeriesById.getStatus());
                if (status == null) {
                    status = new StatusTvSeries(dtoTvSeriesById.getStatus());
                    this.statusTvSeriesRepository.save(status);
                }
                tvSeries.setStatusTvSeries(status);
            }


            List<CrewApiApiDTO> crewDto = dtoTvSeriesById.getCrew().stream().limit(6).toList();
            Set<Crew> crewSet = this.crewService.mapToSet(crewDto, tvSeries);
            crewSet.forEach(crew -> {
                Optional<CrewTvSeries> optional = this.crewTvSeriesRepository.findByTvSeriesIdAndCrewIdAndJobJob(tvSeries.getId(), crew.getId(), "Creator");
                if (optional.isEmpty()) {
                    CrewTvSeries crewTvSeries = new CrewTvSeries();

                    crewTvSeries.setTvSeries(tvSeries);
                    crewTvSeries.setCrew(crew);

                    JobCrew jobByName = this.crewService.findJobByName("Creator");

                    if (jobByName == null) {
                        jobByName = new JobCrew("Creator");
                        this.crewService.saveJob(jobByName);
                    }

                    crewTvSeries.setJob(jobByName);

                    this.crewTvSeriesRepository.save(crewTvSeries);
                }
            });

            MediaResponseCreditsDTO creditsById = getCreditsById(tvSeries.getApiId());
            if (creditsById == null) {
                this.tvSeriesRepository.save(tvSeries);
                continue;
            }

            List<CastApiApiDTO> castDto = this.castService.filterCastApiDto(creditsById);
            Set<Cast> castSet = this.castService.mapToSet(castDto, tvSeries);

            castSet.forEach(cast -> {
                String character = castDto.stream().filter(dto -> dto.getId() == cast.getApiId()).toList().get(0).getCharacter();
                Optional<CastTvSeries> optional = this.castTvSeriesRepository.findByTvSeriesIdAndCastIdAndCharacter(tvSeries.getId(), cast.getId(), character);
                if (optional.isEmpty()) {
                    CastTvSeries castTvSeries = new CastTvSeries();

                    castTvSeries.setTvSeries(tvSeries);
                    castTvSeries.setCast(cast);
                    castTvSeries.setCharacter(character == null || character.isBlank() ? null : character);

                    castTvSeriesRepository.save(castTvSeries);
                }
            });

            this.tvSeriesRepository.save(tvSeries);
        }
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

                if (dto.getPosterPath() == null) {
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

    public MediaResponseCreditsDTO getCreditsById(Long apiId) {
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
