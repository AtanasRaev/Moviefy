package com.moviefy.service.media.movie;

import com.moviefy.config.FetchMediaConfig;
import com.moviefy.config.cache.CacheKeys;
import com.moviefy.database.model.dto.apiDto.mediaDto.MediaApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.MediaResponseCreditsDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieResponseApiDTO;
import com.moviefy.database.model.dto.detailsDto.MovieDetailsDTO;
import com.moviefy.database.model.dto.pageDto.GenrePageDTO;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageWithGenreProjection;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.genre.MovieGenre;
import com.moviefy.database.model.entity.media.Collection;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.service.api.TmdbCommonEndpointService;
import com.moviefy.service.api.movie.TmdbMoviesEndpointService;
import com.moviefy.service.collection.CollectionService;
import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.credit.crew.CrewService;
import com.moviefy.service.genre.movieGenre.MovieGenreService;
import com.moviefy.service.productionCompanies.ProductionCompanyService;
import com.moviefy.utils.GenreNormalizationUtil;
import com.moviefy.utils.MediaValidationUtil;
import com.moviefy.utils.mappers.MovieMapper;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

import static com.moviefy.utils.Ansi.*;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final TmdbMoviesEndpointService tmdbMoviesEndpointService;
    private final TmdbCommonEndpointService tmdbCommonEndpointService;
    private final MovieGenreService movieGenreService;
    private final CastService castService;
    private final CrewService crewService;
    private final CollectionService collectionService;
    private final ProductionCompanyService productionCompanyService;
    private final ModelMapper modelMapper;
    private final MovieMapper movieMapper;
    private final GenreNormalizationUtil genreNormalizationUtil;

    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    public MovieServiceImpl(MovieRepository movieRepository,
                            TmdbMoviesEndpointService tmdbMoviesEndpointService,
                            TmdbCommonEndpointService tmdbCommonEndpointService,
                            MovieGenreService movieGenreService,
                            CastService castService,
                            CrewService crewService,
                            CollectionService collectionService,
                            ProductionCompanyService productionCompanyService,
                            ModelMapper modelMapper,
                            MovieMapper movieMapper,
                            GenreNormalizationUtil genreNormalizationUtil) {
        this.movieRepository = movieRepository;
        this.tmdbMoviesEndpointService = tmdbMoviesEndpointService;
        this.tmdbCommonEndpointService = tmdbCommonEndpointService;
        this.movieGenreService = movieGenreService;
        this.castService = castService;
        this.crewService = crewService;
        this.collectionService = collectionService;
        this.productionCompanyService = productionCompanyService;
        this.modelMapper = modelMapper;
        this.movieMapper = movieMapper;
        this.genreNormalizationUtil = genreNormalizationUtil;
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.LATEST_MOVIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MoviePageProjection> getMoviesFromCurrentMonth(Pageable pageable, List<String> genres) {
        genres = this.genreNormalizationUtil.processMovieGenres(genres);

        return movieRepository.findByReleaseDateAndGenres(
                LocalDate.now().minusDays(7),
                genres,
                pageable
        );
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MOVIE_DETAILS_BY_ID,
            key = "#apiId",
            unless = "#result == null"
    )
    @Transactional(readOnly = true)
    public MovieDetailsDTO getMovieDetailsByApiId(Long apiId) {
        return this.movieRepository.findMovieByApiId(apiId)
                .map(this::mapToMovieDetailsDTO)
                .orElse(null);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.TRENDING_MOVIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MoviePageWithGenreProjection> getTrendingMovies(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.findAllByGenresMapped(processedGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.POPULAR_MOVIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MoviePageWithGenreProjection> getPopularMovies(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.findAllByGenresMapped(processedGenres, pageable);
    }

    @Override
    public boolean isEmpty() {
        return this.movieRepository.count() == 0;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MoviePageWithGenreDTO> searchMovies(String query) {
        MovieResponseApiDTO movieResponseApiDTO = this.tmdbMoviesEndpointService.searchMoviesQueryApi(query);

        if (movieResponseApiDTO == null || movieResponseApiDTO.getResults() == null) {
            return List.of();
        }

        Set<Long> apiIds = movieResponseApiDTO.getResults().stream()
                .map(MediaApiDTO::getId)
                .collect(Collectors.toSet());

        if (apiIds.isEmpty()) {
            return List.of();
        }

        return this.movieRepository.findAllByApiIdIn(apiIds).stream()
                .map(this::mapMoviePageWithGenreDTO)
                .toList();
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MOVIES_BY_GENRES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MoviePageProjection> getMoviesByGenres(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.searchByGenres(processedGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.TOP_RATED_MOVIES,
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MoviePageWithGenreProjection> getTopRatedMovies(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.findTopRatedByGenres(processedGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MOVIES_BY_CAST,
            key = """
                    'cast=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MoviePageProjection> getMoviesByCastId(long id, Pageable pageable) {
        return this.movieRepository.findTopRatedMoviesByCastId(id, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = CacheKeys.MOVIES_BY_CREW,
            key = """
                    'crew=' + #id
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    @Transactional(readOnly = true)
    public Page<MoviePageProjection> getMoviesByCrewId(long id, Pageable pageable) {
        return this.movieRepository.findTopRatedMoviesByCrewId(id, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<MoviePageProjection> getMoviesByProductionCompanyId(long id, Pageable pageable) {
        return this.movieRepository.findTopRatedMoviesByProductionCompanyId(id, pageable);
    }

    private MovieDetailsDTO mapToMovieDetailsDTO(Movie movie) {
        if (movie == null) {
            return null;
        }

        MovieDetailsDTO movieDetails = this.modelMapper.map(movie, MovieDetailsDTO.class);

        movieDetails.setGenres(mapGenres(movie));
        movieDetails.setCast(castService.getCastByMediaId("movie", movie.getId()));
        movieDetails.setCrew(crewService.getCrewByMediaId("movie", movie.getId()));
        movieDetails.setProductionCompanies(this.productionCompanyService.mapProductionCompanies(movie));

        setCollection(movie, movieDetails);

        return movieDetails;
    }

    private Set<GenrePageDTO> mapGenres(Movie movie) {
        return movie.getGenres()
                .stream()
                .sorted(Comparator.comparing(MovieGenre::getId))
                .map(genre -> this.modelMapper.map(genre, GenrePageDTO.class))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void setCollection(Movie movie, MovieDetailsDTO movieDetails) {
        Collection collection = collectionService.getCollectionByMovieId(movie.getId());
        movieDetails.setCollection(getRelatedMoviesInCollection(movie, collection));
        if (collection != null) {
            movieDetails.setCollectionTitle(collection.getName());
        }
    }

    private List<MoviePageWithGenreDTO> getRelatedMoviesInCollection(Movie movie, Collection collection) {
        if (collection == null) {
            return List.of();
        }

        return collection.getMovies()
                .stream()
                .filter(m -> m.getId() != movie.getId())
                .sorted(Comparator.comparing(Movie::getReleaseDate))
                .map(m -> {
                    MoviePageWithGenreDTO map = modelMapper.map(m, MoviePageWithGenreDTO.class);
                    mapOneGenreToPageDTO(map);
                    map.setYear(m.getReleaseDate().getYear());
                    return map;
                })
                .toList();
    }

    private void mapOneGenreToPageDTO(MoviePageWithGenreDTO map) {
        Optional<MovieGenre> optional = this.movieGenreService.getAllGenresByMovieId(map.getId()).stream().findFirst();
        optional.ifPresent(genre -> map.setGenre(genre.getName()));
    }

//    @Scheduled(fixedDelay = 100000000)
    public void fetchMovies() {
        logger.info(CYAN + "Starting to fetch movies..." + RESET);
        int year = FetchMediaConfig.START_YEAR;

        int page = 1;
        Long countNewestMovies = this.movieRepository.countNewestMovies();
        int count = countNewestMovies.intValue();

        if (count > 0) {
            year = this.movieRepository.findNewestMovieYear();

            if (count >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
                year += 1;
                count = 0;
            } else {
                page = (int) Math.ceil(countNewestMovies / FetchMediaConfig.API_MEDIA_PER_PAGE);
            }
        }

        if (year == LocalDate.now().getYear() + 1) {
            return;
        }

        while (count < FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
            logger.info(CYAN + "Movie - Fetching page {} of year {}" + RESET, page, year);

            MovieResponseApiDTO response = this.tmdbMoviesEndpointService.getMoviesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn(YELLOW + "No results returned for page {} of year {}" + RESET, page, year);
                break;
            }

            if (page > response.getTotalPages()) {
                logger.info(CYAN + "Reached the last page for year {}." + RESET, year);
                if (year == LocalDate.now().getYear()) {
                    return;
                }
                year += 1;
                page = 1;
                count = 0;
                continue;
            }

            for (MovieApiDTO dto : response.getResults()) {
                if (count >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
                    break;
                }

                if (MediaValidationUtil.isInvalid(dto)) {
                    logger.warn(YELLOW + "Invalid movie: {}" + RESET, dto.getId());
                    continue;
                }

                if (this.movieRepository.findByApiId(dto.getId()).isPresent()) {
                    logger.info(YELLOW + "Movie already exists: {}" + RESET, dto.getId());
                    continue;
                }

                MovieApiByIdResponseDTO responseById = this.tmdbMoviesEndpointService.getMovieResponseById(dto.getId());

                if (responseById == null || responseById.getRuntime() == null || responseById.getRuntime() < FetchMediaConfig.MIN_MOVIE_RUNTIME) {
                    continue;
                }

                TrailerResponseApiDTO responseTrailer = this.tmdbCommonEndpointService.getTrailerResponseById(
                        dto.getId(),
                        "movie");

                Movie movie = movieMapper.mapToMovie(responseById, responseTrailer);

                if (responseById.getCollection() != null) {
                    Collection collection = this.collectionService.getCollectionFromResponse(responseById.getCollection(), movie);
                    movie.setCollection(collection);
                }

                Map<String, Set<ProductionCompany>> productionCompanyMap = productionCompanyService.getProductionCompaniesFromResponse(responseById, movie);
                movie.setProductionCompanies(productionCompanyMap.get("all"));

                if (!productionCompanyMap.get("toSave").isEmpty()) {
                    this.productionCompanyService.saveAllProduction(productionCompanyMap.get("toSave"));
                }

                this.movieRepository.save(movie);
                count++;

                logger.info(GREEN + "Saved movie: {}" + RESET, movie.getTitle());

                MediaResponseCreditsDTO credits = responseById.getCredits();
                if (credits == null) {
                    continue;
                }

                this.castService.processMovieCast(credits.getCast(), movie);
                this.crewService.processMovieCrew(credits.getCrew(), movie);
            }
            page++;
        }
    }

    private MoviePageWithGenreDTO mapMoviePageWithGenreDTO(Movie movie) {
        MoviePageWithGenreDTO map = this.modelMapper.map(movie, MoviePageWithGenreDTO.class);
        map.setYear(movie.getReleaseDate().getYear());
        mapOneGenreToPageDTO(map);
        return map;
    }
}
