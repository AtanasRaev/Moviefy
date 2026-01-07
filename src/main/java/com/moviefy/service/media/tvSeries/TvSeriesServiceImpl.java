package com.moviefy.service.media.tvSeries;

import com.moviefy.config.FetchMediaConfig;
import com.moviefy.config.cache.CacheKeys;
import com.moviefy.database.model.dto.apiDto.creditDto.CastApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesResponseApiDTO;
import com.moviefy.database.model.dto.detailsDto.SeasonTvSeriesDTO;
import com.moviefy.database.model.dto.detailsDto.TvSeriesDetailsDTO;
import com.moviefy.database.model.dto.pageDto.GenrePageDTO;
import com.moviefy.database.model.dto.pageDto.mediaDto.tvSeriesDto.*;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.genre.SeriesGenre;
import com.moviefy.database.model.entity.media.tvSeries.SeasonTvSeries;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.api.TmdbCommonEndpointService;
import com.moviefy.service.api.tvSeries.TmdbTvEndpointService;
import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.credit.crew.CrewService;
import com.moviefy.service.genre.seriesGenre.SeriesGenreService;
import com.moviefy.service.media.tvSeries.seasons.SeasonsService;
import com.moviefy.service.productionCompanies.ProductionCompanyService;
import com.moviefy.utils.GenreNormalizationUtil;
import com.moviefy.utils.TvSeriesTypesNormalizationUtil;
import com.moviefy.utils.mappers.TvSeriesMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesServiceImpl implements TvSeriesService {
    private final TvSeriesRepository tvSeriesRepository;
    private final TmdbTvEndpointService tmdbTvEndpointService;
    private final TmdbCommonEndpointService tmdbCommonEndpointService;
    private final SeriesGenreService seriesGenreService;
    private final CastService castService;
    private final CrewService crewService;
    private final SeasonsService seasonsService;
    private final ProductionCompanyService productionCompanyService;
    private final ModelMapper modelMapper;
    private final TvSeriesMapper tvSeriesMapper;
    private final GenreNormalizationUtil genreNormalizationUtil;
    private final TvSeriesTypesNormalizationUtil tvSeriesTypesNormalizationUtil;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesServiceImpl.class);

    public TvSeriesServiceImpl(TvSeriesRepository tvSeriesRepository,
                               TmdbTvEndpointService tmdbTvEndpointService,
                               TmdbCommonEndpointService tmdbCommonEndpointService,
                               SeriesGenreService seriesGenreService,
                               CastService castService,
                               CrewService crewService,
                               SeasonsService seasonsService,
                               ProductionCompanyService productionCompanyService,
                               ModelMapper modelMapper,
                               TvSeriesMapper tvSeriesMapper,
                               GenreNormalizationUtil genreNormalizationUtil,
                               TvSeriesTypesNormalizationUtil tvSeriesTypesNormalizationUtil) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
        this.tmdbCommonEndpointService = tmdbCommonEndpointService;
        this.seriesGenreService = seriesGenreService;
        this.castService = castService;
        this.crewService = crewService;
        this.seasonsService = seasonsService;
        this.productionCompanyService = productionCompanyService;
        this.modelMapper = modelMapper;
        this.tvSeriesMapper = tvSeriesMapper;
        this.genreNormalizationUtil = genreNormalizationUtil;
        this.tvSeriesTypesNormalizationUtil = tvSeriesTypesNormalizationUtil;
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.LATEST_TV_SERIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processSeriesGenres(#genres))
                    + ';t=' + T(java.lang.String).join(',', @tvSeriesTypesNormalizationUtil.processTypes(#types))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<TvSeriesPageProjection> getTvSeriesFromCurrentMonth(Pageable pageable, List<String> genres, List<String> types) {
        genres = this.genreNormalizationUtil.processSeriesGenres(genres);
        types = this.tvSeriesTypesNormalizationUtil.processTypes(types);

        return this.tvSeriesRepository.findByFirstAirDateAndGenres(
                LocalDate.now(),
                genres,
                types,
                pageable
        );
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.TV_SERIES_DETAILS_BY_ID,
            key = "#apiId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public TvSeriesDetailsDTO getTvSeriesDetailsByApiId(Long apiId) {
        return this.tvSeriesRepository.findTvSeriesByApiId(apiId)
                .map(this::mapToTvSeriesDetailsDTO)
                .orElse(null);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.TRENDING_TV_SERIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processSeriesGenres(#genres))
                    + ';t=' + T(java.lang.String).join(',', @tvSeriesTypesNormalizationUtil.processTypes(#types))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<TvSeriesPageWithGenreProjection> getTrendingTvSeries(List<String> genres, List<String> types, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processSeriesGenres(genres);
        List<String> processedTypes = this.tvSeriesTypesNormalizationUtil.processTypes(types);

        return this.tvSeriesRepository.findAllByGenresMapped(processedGenres, processedTypes, pageable);
    }

    @Override
    @Cacheable(cacheNames = CacheKeys.POPULAR_TV_SERIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processSeriesGenres(#genres))
                    + ';t=' + T(java.lang.String).join(',', @tvSeriesTypesNormalizationUtil.processTypes(#types))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<TvSeriesPageWithGenreProjection> getPopularTvSeries(List<String> genres, List<String> types, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processSeriesGenres(genres);
        List<String> processedTypes = this.tvSeriesTypesNormalizationUtil.processTypes(types);

        return this.tvSeriesRepository.findAllByGenresMapped(processedGenres, processedTypes, pageable);
    }

    @Override
    public boolean isEmpty() {
        return this.tvSeriesRepository.count() == 0;
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.HOME_SERIES_BY_COLLECTION,
            key = "#input",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public List<TvSeriesTrendingPageDTO> getHomeSeriesDTO(List<String> input) {
        return this.tvSeriesRepository.findAllByNames(input)
                .stream()
                .map(tvSeries -> {
                    TvSeriesTrendingPageDTO map = this.modelMapper.map(tvSeries, TvSeriesTrendingPageDTO.class);
                    mapAllGenresToPageDTO(map);
                    mapSeasonsToPageDTO(this.seasonsService.findAllByTvSeriesId(map.getId()), map);
                    return map;
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<TvSeriesPageWithGenreDTO> searchTvSeries(String query) {
        TvSeriesResponseApiDTO tvSeriesResponseApiDTO = this.tmdbTvEndpointService.searchTvSeriesQueryApi(query);

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
            cacheNames = CacheKeys.TV_SERIES_BY_GENRES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processSeriesGenres(#genres))
                    + ';t=' + T(java.lang.String).join(',', @tvSeriesTypesNormalizationUtil.processTypes(#types))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<TvSeriesPageProjection> getTvSeriesByGenres(List<String> genres, List<String> types, Pageable pageable) {
        List<String> lowerCaseGenres = this.genreNormalizationUtil.processSeriesGenres(genres);
        List<String> loweredTypes = this.tvSeriesTypesNormalizationUtil.processTypes(types);

        return tvSeriesRepository.searchByGenres(lowerCaseGenres, loweredTypes, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.TOP_RATED_TV_SERIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processSeriesGenres(#genres))
                    + ';t=' + T(java.lang.String).join(',', @tvSeriesTypesNormalizationUtil.processTypes(#types))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<TvSeriesPageWithGenreProjection> getTopRatedTvSeries(List<String> genres, List<String> types, Pageable pageable) {
        List<String> lowerCaseGenres = this.genreNormalizationUtil.processSeriesGenres(genres);
        List<String> loweredTypes = this.tvSeriesTypesNormalizationUtil.processTypes(types);
        return tvSeriesRepository.findTopRatedSeriesByGenresAndTypes(lowerCaseGenres, loweredTypes, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.SERIES_BY_CAST,
            key = """
                    'cast=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<TvSeriesPageProjection> getTvSeriesByCastId(long id, Pageable pageable) {
        return this.tvSeriesRepository.findTopRatedSeriesByCastId(id, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.SERIES_BY_CREW,
            key = """
                    'crew=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<TvSeriesPageProjection> getTvSeriesByCrewId(long id, Pageable pageable) {
        return this.tvSeriesRepository.findTopRatedSeriesByCrewId(id, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<TvSeriesPageProjection> getTvSeriesByProductionCompanyId(long id, Pageable pageable) {
        return this.tvSeriesRepository.findTopRatedSeriesByProductionCompanyId(id, pageable);
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
    public void fetchSeries() {
        logger.info(BLUE + "Starting to fetch tv series..." + RESET);
        int year = FetchMediaConfig.START_YEAR;

        int page = 1;
        Long countNewestTvSeries = this.tvSeriesRepository.countNewestTvSeries();
        int count = countNewestTvSeries.intValue();

        if (count > 0) {
            year = this.tvSeriesRepository.findNewestTvSeriesYear();

            if (count >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
                year += 1;
                count = 0;
            } else {
                page = (int) Math.ceil(count / FetchMediaConfig.API_MEDIA_PER_PAGE);
            }
        }

        if (year == LocalDate.now().getYear() + 1) {
            return;
        }

        while (count < FetchMediaConfig.MAX_MEDIA_PER_YEAR) {

            logger.info(BLUE + "TV series - Fetching page {} of year {}" + RESET, page, year);

            TvSeriesResponseApiDTO response = this.tmdbTvEndpointService.getTvSeriesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn(YELLOW + "No results returned for page {} of year {}" + RESET, page, year);
                break;
            }

            if (page > response.getTotalPages()) {
                logger.info(BLUE + "Reached the last page for year {}." + RESET, year);
                if (year == LocalDate.now().getYear()) {
                    return;
                }
                year += 1;
                page = 1;
                count = 0;
                continue;
            }

            for (TvSeriesApiDTO dto : response.getResults()) {

                if (count >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
                    break;
                }

                if (isInvalid(dto)) {
                    logger.warn(YELLOW + "Invalid TV series: {}" + RESET, dto.getId());
                    continue;
                }

                if (this.tvSeriesRepository.findByApiId(dto.getId()).isPresent()) {
                    logger.info(YELLOW + "TV series already exists: {}" + RESET, dto.getId());
                    continue;
                }

//                if (dto.getId() == 96444) {
//                    continue;
//                }

                TvSeriesApiByIdResponseDTO responseById = this.tmdbTvEndpointService.getTvSeriesResponseById(dto.getId());

                if (responseById == null || responseById.getType() == null) {
                    continue;
                }

                if (!responseById.getType().equalsIgnoreCase("scripted")
                        && !responseById.getType().equalsIgnoreCase("reality")
                        && !responseById.getType().equalsIgnoreCase("documentary")
                        && !responseById.getType().equalsIgnoreCase("miniseries")
                        && !responseById.getType().equalsIgnoreCase("animation")) {
                    logger.warn(YELLOW + "Invalid TV series type: id-{} type-{}" + RESET, dto.getId(), responseById.getType());
                    continue;
                }

                TrailerResponseApiDTO responseTrailer = this.tmdbCommonEndpointService.getTrailerResponseById(dto.getId(), "tv");

                TvSeries tvSeries = this.tvSeriesMapper.mapToTvSeries(responseById, responseTrailer);

                Map<String, Set<ProductionCompany>> productionCompaniesMap =
                        this.productionCompanyService.getProductionCompaniesFromResponse(responseById, tvSeries);

                tvSeries.setProductionCompanies(productionCompaniesMap.get("all"));
                if (!productionCompaniesMap.get("toSave").isEmpty()) {
                    this.productionCompanyService.saveAllProduction(productionCompaniesMap.get("toSave"));
                }

                tvSeries.setSeasons(this.seasonsService.mapSeasonsAndEpisodesFromResponse(responseById.getSeasons(), tvSeries));
                this.tvSeriesRepository.save(tvSeries);
                count++;

                logger.info(PURPLE + "Saved tv series: {}" + RESET, tvSeries.getName());

                this.crewService.processTvSeriesCrew(responseById.getCrew(), tvSeries);

                Set<CastApiDTO> cast = responseById.getCredits().getCast();
                if (cast == null || cast.isEmpty()) {
                    continue;
                }

                this.castService.processTvSeriesCast(cast, tvSeries);
            }

            page++;
        }
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

    private TvSeriesPageWithGenreDTO mapTvSeriesPageWithGenreDTO(TvSeries tvSeries) {
        TvSeriesPageWithGenreDTO map = this.modelMapper.map(tvSeries, TvSeriesPageWithGenreDTO.class);
        map.setYear(tvSeries.getFirstAirDate().getYear());
        mapOneGenreToPageDTO(map);
        mapSeasonsToPageDTO(this.seasonsService.findAllByTvSeriesId(map.getId()), map);
        return map;
    }
}
