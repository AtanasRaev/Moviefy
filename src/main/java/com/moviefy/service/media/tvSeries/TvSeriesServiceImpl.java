package com.moviefy.service.media.tvSeries;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.*;
import com.moviefy.database.model.dto.databaseDto.EpisodeDTO;
import com.moviefy.database.model.dto.databaseDto.SeasonDTO;
import com.moviefy.database.model.dto.detailsDto.SeasonTvSeriesDTO;
import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.GenrePageDTO;
import com.moviefy.database.model.dto.pageDto.tvSeriesDto.*;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.credit.cast.Cast;
import com.moviefy.database.model.entity.credit.cast.CastTvSeries;
import com.moviefy.database.model.entity.credit.crew.Crew;
import com.moviefy.database.model.entity.credit.crew.CrewTvSeries;
import com.moviefy.database.model.entity.genre.SeriesGenre;
import com.moviefy.database.model.entity.media.tvSeries.EpisodeTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.SeasonTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.credit.cast.CastTvSeriesRepository;
import com.moviefy.database.repository.credit.crew.CrewTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.EpisodeTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.SeasonTvSeriesRepository;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.credit.crew.CrewService;
import com.moviefy.service.genre.seriesGenre.SeriesGenreService;
import com.moviefy.service.productionCompanies.ProductionCompanyService;
import com.moviefy.utils.GenreNormalizationUtil;
import com.moviefy.utils.TrailerMappingUtil;
import com.moviefy.utils.TvSeriesMapper;
import com.moviefy.utils.TvSeriesTypesNormalizationUtil;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private final EpisodeTvSeriesRepository episodeTvSeriesRepository;
    private final ProductionCompanyService productionCompanyService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final TrailerMappingUtil trailerMappingUtil;
    private final ModelMapper modelMapper;
    private final TvSeriesMapper tvSeriesMapper;
    private final GenreNormalizationUtil genreNormalizationUtil;
    private final TvSeriesTypesNormalizationUtil tvSeriesTypesNormalizationUtil;
    private static final Logger logger = LoggerFactory.getLogger(TvSeriesServiceImpl.class);
    private static final int START_YEAR = 1970;
    private static final int MAX_TV_SERIES_PER_YEAR = 600;
    private static final double API_TV_SERIES_PER_PAGE = 20.0;

    public TvSeriesServiceImpl(TvSeriesRepository tvSeriesRepository,
                               CrewTvSeriesRepository crewTvSeriesRepository,
                               CastTvSeriesRepository castTvSeriesRepository,
                               SeriesGenreService seriesGenreService,
                               CastService castService,
                               CrewService crewService,
                               SeasonTvSeriesRepository seasonTvSeriesRepository,
                               EpisodeTvSeriesRepository episodeTvSeriesRepository,
                               ProductionCompanyService productionCompanyService,
                               ApiConfig apiConfig,
                               RestClient restClient,
                               TrailerMappingUtil trailerMappingUtil,
                               ModelMapper modelMapper,
                               TvSeriesMapper tvSeriesMapper,
                               GenreNormalizationUtil genreNormalizationUtil,
                               TvSeriesTypesNormalizationUtil tvSeriesTypesNormalizationUtil) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.crewTvSeriesRepository = crewTvSeriesRepository;
        this.castTvSeriesRepository = castTvSeriesRepository;
        this.seriesGenreService = seriesGenreService;
        this.castService = castService;
        this.crewService = crewService;
        this.seasonTvSeriesRepository = seasonTvSeriesRepository;
        this.episodeTvSeriesRepository = episodeTvSeriesRepository;
        this.productionCompanyService = productionCompanyService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.trailerMappingUtil = trailerMappingUtil;
        this.modelMapper = modelMapper;
        this.tvSeriesMapper = tvSeriesMapper;
        this.genreNormalizationUtil = genreNormalizationUtil;
        this.tvSeriesTypesNormalizationUtil = tvSeriesTypesNormalizationUtil;
    }

    @Override
    @Cacheable(
            cacheNames = "latestTvSeries",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processSeriesGenres(#genres))
                    + ';t=' + T(java.lang.String).join(',', @tvSeriesTypesNormalizationUtil.processTypes(#types))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<TvSeriesPageProjection> getTvSeriesFromCurrentMonth(Pageable pageable, List<String> genres, List<String> types) {
        genres = this.genreNormalizationUtil.processSeriesGenres(genres);
        types = this.tvSeriesTypesNormalizationUtil.processTypes(types);

        return this.tvSeriesRepository.findByFirstAirDateAndGenres(
                getStartOfCurrentMonth(),
                genres,
                types,
                pageable
        );
    }

    @Override
    @Cacheable(
            cacheNames = "tvSeriesDetailsById",
            key = "#apiId",
            unless = "#result == null"
    )
    public TvSeriesDetailsDTO getTvSeriesDetailsByApiId(Long apiId) {
        return this.tvSeriesRepository.findTvSeriesByApiId(apiId)
                .map(this::mapToTvSeriesDetailsDTO)
                .orElse(null);
    }

    @Override
    @Cacheable(
            cacheNames = "trendingTvSeries",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processSeriesGenres(#genres))
                    + ';t=' + T(java.lang.String).join(',', @tvSeriesTypesNormalizationUtil.processTypes(#types))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<TvSeriesPageWithGenreProjection> getTrendingTvSeries(List<String> genres, List<String> types, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processSeriesGenres(genres);
        List<String> processedTypes = this.tvSeriesTypesNormalizationUtil.processTypes(types);

        return this.tvSeriesRepository.findAllByPopularityDesc(processedGenres, processedTypes, pageable);
    }

    @Override
    @Cacheable(cacheNames = "popularTvSeries",
            key = "'p=' + #pageable.pageNumber + ';s=' + #pageable.pageSize + ';sort=' + T(java.util.Objects).toString(#pageable.sort)",
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<TvSeriesPageWithGenreDTO> getPopularTvSeries(Pageable pageable) {
        return this.tvSeriesRepository.findAllSortedByVoteCount(pageable)
                .map(this::mapTvSeriesPageWithGenreDTO);
    }

    @Override
    public boolean isEmpty() {
        return this.tvSeriesRepository.count() == 0;
    }

    @Override
    @Cacheable(
            cacheNames = "homeSeriesByCollection",
            key = "#input",
            unless = "#result == null"
    )
    public List<TvSeriesTrendingPageDTO> getHomeSeriesDTO(List<String> input) {
        return this.tvSeriesRepository.findAllByNames(input)
                .stream()
                .map(tvSeries -> {
                    TvSeriesTrendingPageDTO map = this.modelMapper.map(tvSeries, TvSeriesTrendingPageDTO.class);
                    mapAllGenresToPageDTO(map);
                    mapSeasonsToPageDTO(new HashSet<>(this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId())), map);
                    return map;
                })
                .toList();
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
    public List<TvSeriesPageWithGenreDTO> searchTvSeries(String query) {
        TvSeriesResponseApiDTO tvSeriesResponseApiDTO = this.searchQueryApi(query);

        if (tvSeriesResponseApiDTO == null || tvSeriesResponseApiDTO.getResults() == null) {
            return List.of();
        }

        Set<Long> apiIds = tvSeriesResponseApiDTO.getResults().stream()
                .map(TvSeriesApiDTO::getId)
                .collect(Collectors.toSet());

        if (apiIds.isEmpty()) {
            return List.of();
        }

        return this.tvSeriesRepository.findAllByApiIdIn(apiIds).stream()
                .map(this::mapTvSeriesPageWithGenreDTO)
                .toList();
    }

    @Override
    @Cacheable(
            cacheNames = "tvSeriesByGenres",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.getSeriesLowerCaseGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<TvSeriesPageProjection> getTvSeriesByGenres(List<String> genres, Pageable pageable) {
        List<String> lowerCaseGenres = this.genreNormalizationUtil.getSeriesLowerCaseGenres(genres);

        return tvSeriesRepository.searchByGenres(lowerCaseGenres, pageable);
    }

    private TvSeriesPageDTO mapTvSeriesPageDTO(TvSeries tvSeries) {
        TvSeriesPageDTO map = modelMapper.map(tvSeries, TvSeriesPageDTO.class);
        map.setYear(tvSeries.getFirstAirDate().getYear());
        List<SeasonTvSeries> seasons = this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId());
        if (!seasons.isEmpty()) {
            mapSeasonsToPageDTO(new HashSet<>(seasons), map);
        }
        return map;
    }

    private TvSeriesDetailsDTO mapToTvSeriesDetailsDTO(TvSeries tvSeries) {
        if (tvSeries == null) {
            return null;
        }

        TvSeriesDetailsDTO tvDetails = this.modelMapper.map(tvSeries, TvSeriesDetailsDTO.class);

        tvDetails.setGenres(mapGenres(tvSeries));
        tvDetails.setCast(this.castService.getCastByMediaId("tv", tvDetails.getId()));
        tvDetails.setCrew(this.crewService.getCrewByMediaId("tv", tvDetails.getId()));
        tvDetails.setProductionCompanies(this.productionCompanyService.mapProductionCompanies(tvSeries));
        sortSeasons(tvDetails);

        return tvDetails;
    }

    private Set<GenrePageDTO> mapGenres(TvSeries tvSeries) {
        return tvSeries.getGenres()
                .stream()
                .sorted(Comparator.comparing(SeriesGenre::getId))
                .map(genre -> this.modelMapper.map(genre, GenrePageDTO.class))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void sortSeasons(TvSeriesDetailsDTO tv) {
        tv.setSeasons(tv.getSeasons()
                .stream()
                .sorted(Comparator.comparingInt(SeasonTvSeriesDTO::getSeasonNumber))
                .collect(Collectors.toCollection(LinkedHashSet::new)));
    }

    //    @Scheduled(fixedDelay = 100000000)
    //TODO
    private void updateTvSeries() {
    }

    private LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().minusDays(7);
    }

    //    @Scheduled(fixedDelay = 100)
    public void fetchSeries() {
        logger.info("\u001B[36mStarting to fetch tv series...\u001B[0m");
        LocalDateTime start = LocalDateTime.now();

        int year = START_YEAR;

        int page = 1;
        Long countNewestTvSeries = this.tvSeriesRepository.countNewestTvSeries();
        int count = countNewestTvSeries.intValue();

        if (count > 0) {
            year = this.tvSeriesRepository.findNewestTvSeriesYear();

            if (count >= MAX_TV_SERIES_PER_YEAR) {
                year += 1;
                count = 0;
            } else {
                page = (int) Math.ceil(count / API_TV_SERIES_PER_PAGE);
            }
        }

        if (year == LocalDate.now().getYear() + 1) {
            return;
        }

        while (count < MAX_TV_SERIES_PER_YEAR) {

            logger.info("\u001B[36mTV series - Fetching page {} of year {}\u001B[0m", page, year);

            TvSeriesResponseApiDTO response = getTvSeriesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn("\u001B[33mNo results returned for page {} of year {}\u001B[0m", page, year);
                break;
            }

            if (page > response.getTotalPages()) {
                logger.info("\u001B[36mReached the last page for year {}.\u001B[0m", year);
                if (year == LocalDate.now().getYear()) {
                    return;
                }
                year += 1;
                page = 1;
                count = 0;
                continue;
            }

            for (TvSeriesApiDTO dto : response.getResults()) {

                if (count >= MAX_TV_SERIES_PER_YEAR) {
                    break;
                }

                if (isInvalid(dto)) {
                    logger.warn("\u001B[33mInvalid TV series: {}\u001B[0m", dto.getId());
                    continue;
                }

                if (this.tvSeriesRepository.findByApiId(dto.getId()).isPresent()) {
                    logger.info("\u001B[36mTV series already exists: {}\u001B[0m", dto.getId());
                    continue;
                }

//                if (dto.getId() == 96444) {
//                    continue;
//                }

                TvSeriesApiByIdResponseDTO responseById = getTvSeriesResponseById(dto.getId());

                if (responseById == null || responseById.getType() == null) {
                    continue;
                }

                if (!responseById.getType().equalsIgnoreCase("scripted")
                        && !responseById.getType().equalsIgnoreCase("reality")
                        && !responseById.getType().equalsIgnoreCase("documentary")
                        && !responseById.getType().equalsIgnoreCase("miniseries")
                        && !responseById.getType().equalsIgnoreCase("animation")) {
                    logger.warn("\u001B[33mInvalid TV series type: id-{} type-{}\u001B[0m", dto.getId(), responseById.getType());
                    continue;
                }

                TrailerResponseApiDTO responseTrailer = this.trailerMappingUtil.getTrailerResponseById(
                        dto.getId(),
                        this.apiConfig.getUrl(),
                        this.apiConfig.getKey(),
                        "tv");

                TvSeries tvSeries = this.tvSeriesMapper.mapToTvSeries(dto, responseById, responseTrailer);

                Map<String, Set<ProductionCompany>> productionCompaniesMap =
                        this.productionCompanyService.getProductionCompaniesFromResponse(responseById, tvSeries);

                tvSeries.setProductionCompanies(productionCompaniesMap.get("all"));

                if (!productionCompaniesMap.get("toSave").isEmpty()) {
                    this.productionCompanyService.saveAllProduction(productionCompaniesMap.get("toSave"));
                }

                tvSeries.setSeasons(mapSeasonsAndEpisodesFromResponse(responseById.getSeasons(), tvSeries));

                this.tvSeriesRepository.save(tvSeries);
                count++;

                logger.info("\u001B[36mSaved tv series: {}\u001B[0m", tvSeries.getName());

                List<CrewApiDTO> crewDto = responseById.getCrew().stream().limit(6).toList();
                Set<Crew> crewSet = this.crewService.mapToSet(crewDto);
                processTvSeriesCrew(crewDto, tvSeries, crewSet);

                Set<CastApiDTO> cast = responseById.getCredits().getCast();

                if (cast == null || cast.isEmpty()) {
                    continue;
                }

                List<CastApiDTO> castDto = this.castService.filterCastApiDto(cast);
                Set<Cast> castSet = this.castService.mapToSet(castDto);
                processTvSeriesCast(castDto, tvSeries, castSet);
            }

            page++;
        }

        LocalDateTime end = LocalDateTime.now();
        logger.info("\u001B[36mFinished fetching tv series for {}.\u001B[0m", formatDurationLong(Duration.between(start, end)));
    }

    public static String formatDurationLong(Duration duration) {
        long millis = duration.toMillis();

        long days = millis / 86_400_000;
        millis %= 86_400_000;

        long hours = millis / 3_600_000;
        millis %= 3_600_000;

        long minutes = millis / 60_000;
        millis %= 60_000;

        long seconds = millis / 1_000;
        millis %= 1_000;

        StringBuilder sb = new StringBuilder();

        if (days > 0) sb.append(days).append("d ");
        if (hours > 0 || days > 0) sb.append(hours).append("h ");
        if (minutes > 0 || hours > 0 || days > 0) sb.append(minutes).append("m ");
        if (seconds > 0 || minutes > 0 || hours > 0 || days > 0) sb.append(seconds).append("s ");
        sb.append(millis).append("ms");

        return sb.toString().trim();
    }


    private static boolean isInvalid(TvSeriesApiDTO dto) {
        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || dto.getName() == null || dto.getName().isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }

    private static void mapSeasonsToPageDTO(Set<SeasonTvSeries> seasons, TvSeriesDTO map) {
        seasons.stream()
                .max(Comparator.comparing(SeasonTvSeries::getSeasonNumber))
                .ifPresent(lastSeason -> {
                    map.setSeasonsCount(lastSeason.getSeasonNumber() > 411
                            ? lastSeason.getSeasonNumber()
                            : seasons.size());
                    map.setEpisodesCount(lastSeason.getEpisodeCount());
                });
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
        EpisodesTvSeriesResponseDTO episodesResponse = getEpisodesResponse(id, season.getSeasonNumber());

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

    private void mapOneGenreToPageDTO(TvSeriesPageWithGenreDTO map) {
        Optional<SeriesGenre> optional = this.seriesGenreService.getAllGenresByMovieId(map.getId()).stream().findFirst();
        optional.ifPresent(genre -> map.setGenre(genre.getName()));
    }

    private void mapAllGenresToPageDTO(TvSeriesTrendingPageDTO map) {
        List<SeriesGenre> genres = this.seriesGenreService.getAllGenresByMovieId(map.getId()).stream().toList();
        if (!genres.isEmpty()) {
            map.setAllGenres(genres
                    .stream()
                    .map(genre -> this.modelMapper.map(genre, GenrePageDTO.class)).toList());
        }
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

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching tv-series" + "- " + e.getMessage());
            return null;
        }
    }

//    private SeasonTvSeriesResponseApiDTO getSeasonsResponse(Long id) {
//        String url = String.format(this.apiConfig.getUrl() + "/tv/%d?api_key=%s", id, this.apiConfig.getKey());
//
//        try {
//            return this.restClient
//                    .get()
//                    .uri(url)
//                    .retrieve()
//                    .body(SeasonTvSeriesResponseApiDTO.class);
//        } catch (Exception e) {
//            System.err.println("Error fetching season for tv-series with ID: " + id + " - " + e.getMessage());
//            return null;
//        }
//    }

    private EpisodesTvSeriesResponseDTO getEpisodesResponse(Long tvId, Integer season) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d/season/%d?api_key=%s", tvId, season, this.apiConfig.getKey());

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(EpisodesTvSeriesResponseDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching season for tv-series with ID: " + tvId + " - " + e.getMessage());
            return null;
        }
    }

    private TvSeriesApiByIdResponseDTO getTvSeriesResponseById(Long apiId) {
        String url = String.format(this.apiConfig.getUrl() + "/tv/%d?api_key=" + this.apiConfig.getKey() + "&append_to_response=credits,external_ids", apiId);
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

//    private MediaResponseCreditsDTO getCreditsById(Long apiId) {
//        String url = String.format(this.apiConfig.getUrl() + "/tv/%d/credits?api_key=%s", apiId, this.apiConfig.getKey());
//
//        try {
//            return this.restClient
//                    .get()
//                    .uri(url)
//                    .retrieve()
//                    .body(MediaResponseCreditsDTO.class);
//        } catch (Exception e) {
//            System.err.println("Error fetching credits with ID: " + apiId + " - " + e.getMessage());
//            return null;
//        }
//    }

    private TvSeriesResponseApiDTO searchQueryApi(String query) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/search/tv?api_key=%s&page=1&query=%s",
                this.apiConfig.getKey(), query);

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(TvSeriesResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error searching movies" + "- " + e.getMessage());
            return null;
        }

    }

    private TvSeriesPageWithGenreDTO mapTvSeriesPageWithGenreDTO(TvSeries tvSeries) {
        TvSeriesPageWithGenreDTO map = this.modelMapper.map(tvSeries, TvSeriesPageWithGenreDTO.class);
        map.setYear(tvSeries.getFirstAirDate().getYear());
        mapOneGenreToPageDTO(map);
        mapSeasonsToPageDTO(new HashSet<>(this.seasonTvSeriesRepository.findAllByTvSeriesId(map.getId())), map);
        return map;
    }
}
