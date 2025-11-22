package com.moviefy.service.media.movie;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.*;
import com.moviefy.database.model.dto.detailsDto.MovieDetailsDTO;
import com.moviefy.database.model.dto.pageDto.GenrePageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageProjection;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.MoviePageWithGenreProjection;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.credit.cast.Cast;
import com.moviefy.database.model.entity.credit.cast.CastMovie;
import com.moviefy.database.model.entity.credit.crew.Crew;
import com.moviefy.database.model.entity.credit.crew.CrewMovie;
import com.moviefy.database.model.entity.genre.MovieGenre;
import com.moviefy.database.model.entity.media.Collection;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.credit.cast.CastMovieRepository;
import com.moviefy.database.repository.credit.crew.CrewMovieRepository;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.service.collection.CollectionService;
import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.credit.crew.CrewService;
import com.moviefy.service.genre.movieGenre.MovieGenreService;
import com.moviefy.service.productionCompanies.ProductionCompanyService;
import com.moviefy.utils.GenreNormalizationUtil;
import com.moviefy.utils.MovieMapper;
import com.moviefy.utils.TrailerMappingUtil;
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
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final CastMovieRepository castMovieRepository;
    private final CrewMovieRepository crewMovieRepository;
    private final MovieGenreService movieGenreService;
    private final CastService castService;
    private final CrewService crewService;
    private final CollectionService collectionService;
    private final ProductionCompanyService productionCompanyService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final ModelMapper modelMapper;
    private final TrailerMappingUtil trailerMappingUtil;
    private final MovieMapper movieMapper;
    private final GenreNormalizationUtil genreNormalizationUtil;
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);
    private static final int START_YEAR = 1970;
    private static final int MAX_MOVIES_PER_YEAR = 600;
    private static final int MIN_MOVIE_RUNTIME = 45;
    private static final double API_MOVIES_PER_PAGE = 20.0;

    public MovieServiceImpl(MovieRepository movieRepository,
                            CastMovieRepository castMovieRepository,
                            CrewMovieRepository crewMovieRepository,
                            MovieGenreService movieGenreService,
                            CastService castService,
                            CrewService crewService,
                            CollectionService collectionService,
                            ProductionCompanyService productionCompanyService,
                            ApiConfig apiConfig,
                            RestClient restClient,
                            ModelMapper modelMapper,
                            TrailerMappingUtil trailerMappingUtil,
                            MovieMapper movieMapper,
                            GenreNormalizationUtil genreNormalizationUtil) {
        this.movieRepository = movieRepository;
        this.castMovieRepository = castMovieRepository;
        this.crewMovieRepository = crewMovieRepository;
        this.movieGenreService = movieGenreService;
        this.castService = castService;
        this.crewService = crewService;
        this.collectionService = collectionService;
        this.productionCompanyService = productionCompanyService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.modelMapper = modelMapper;
        this.trailerMappingUtil = trailerMappingUtil;
        this.movieMapper = movieMapper;
        this.genreNormalizationUtil = genreNormalizationUtil;
    }

    @Override
    @Cacheable(
            cacheNames = "latestMovies",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MoviePageProjection> getMoviesFromCurrentMonth(Pageable pageable, List<String> genres) {
        genres = this.genreNormalizationUtil.processMovieGenres(genres);

        return movieRepository.findByReleaseDateAndGenres(
                getStartOfCurrentMonth(),
                genres,
                pageable
        );
    }

    @Override
    @Cacheable(
            cacheNames = "movieDetailsById",
            key = "#apiId",
            unless = "#result == null"
    )
    public MovieDetailsDTO getMovieDetailsByApiId(Long apiId) {
        return this.movieRepository.findMovieByApiId(apiId)
                .map(this::mapToMovieDetailsDTO)
                .orElse(null);
    }

    @Override
    @Cacheable(
            cacheNames = "trendingMovies",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MoviePageWithGenreProjection> getTrendingMovies(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.findAllByGenresMapped(processedGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "popularMovies",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MoviePageWithGenreProjection> getPopularMovies(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.findAllByGenresMapped(processedGenres, pageable);
    }

    @Override
    public boolean isEmpty() {
        return this.movieRepository.count() == 0;
    }

    @Override
    public List<MoviePageWithGenreDTO> searchMovies(String query) {
        MovieResponseApiDTO movieResponseApiDTO = this.searchQueryApi(query);

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
            cacheNames = "moviesByGenres",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MoviePageProjection> getMoviesByGenres(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.searchByGenres(processedGenres, pageable);
    }

    @Override
    @Cacheable(
            cacheNames = "topRatedMovies",
            key = """
                    'g=' + T(java.lang.String).join(',', @genreNormalizationUtil.processMovieGenres(#genres))
                    + ';p=' + #pageable.pageNumber
                    + ';s=' + #pageable.pageSize
                    + ';sort=' + T(java.util.Objects).toString(#pageable.sort)
                    """,
            unless = "#result == null || #result.isEmpty()"
    )
    public Page<MoviePageWithGenreProjection> getTopRatedMovies(List<String> genres, Pageable pageable) {
        List<String> processedGenres = this.genreNormalizationUtil.processMovieGenres(genres);
        return this.movieRepository.findTopRatedByGenres(processedGenres, pageable);
    }

    private LocalDate getStartOfCurrentMonth() {
        return LocalDate.now().minusDays(7);
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
    //TODO
    private void updateMovies() {
        logger.info("Starting to update movies...");


    }

//    @Scheduled(fixedDelay = 100)
    public void fetchMovies() {
        logger.info("\u001B[32mStarting to fetch movies...\u001B[0m");
        LocalDateTime start = LocalDateTime.now();

        int year = START_YEAR;

        int page = 1;
        Long countNewestMovies = this.movieRepository.countNewestMovies();
        int count = countNewestMovies.intValue();

        if (count > 0) {
            year = this.movieRepository.findNewestMovieYear();

            if (count >= MAX_MOVIES_PER_YEAR) {
                year += 1;
                count = 0;
            } else {
                page = (int) Math.ceil(countNewestMovies / API_MOVIES_PER_PAGE);
            }
        }

        if (year == LocalDate.now().getYear() + 1) {
            return;
        }

        while (count < MAX_MOVIES_PER_YEAR) {
            logger.info("\u001B[32mMovie - Fetching page {} of year {}\u001B[0m", page, year);

            MovieResponseApiDTO response = getMoviesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn("\u001B[33mNo results returned for page {} of year {}\u001B[0m", page, year);
                break;
            }

            if (page > response.getTotalPages()) {
                logger.info("\u001B[32mReached the last page for year {}.\u001B[0m", year);
                if (year == LocalDate.now().getYear()) {
                    return;
                }
                year += 1;
                page = 1;
                count = 0;
                continue;
            }

            for (MovieApiDTO dto : response.getResults()) {
                if (count >= MAX_MOVIES_PER_YEAR) {
                    break;
                }

                if (isInvalid(dto)) {
                    logger.warn("\u001B[33mInvalid movie: {}\u001B[0m", dto.getId());
                    continue;
                }

                if (this.movieRepository.findByApiId(dto.getId()).isPresent()) {
                    logger.info("\u001B[32mMovie already exists: {}\u001B[0m", dto.getId());
                    continue;
                }

                MovieApiByIdResponseDTO responseById = getMediaResponseById(dto.getId());

                if (responseById == null || responseById.getRuntime() == null || responseById.getRuntime() < MIN_MOVIE_RUNTIME) {
                    continue;
                }

                TrailerResponseApiDTO responseTrailer = trailerMappingUtil.getTrailerResponseById(
                        dto.getId(),
                        this.apiConfig.getUrl(),
                        this.apiConfig.getKey(),
                        "movie");

                Movie movie = movieMapper.mapToMovie(dto, responseById, responseTrailer);

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

                logger.info("\u001B[32mSaved movie: {}\u001B[0m", movie.getTitle());

                MediaResponseCreditsDTO creditsById = responseById.getCredits();

                if (creditsById == null) {
                    continue;
                }

                List<CastApiDTO> castDto = this.castService.filterCastApiDto(creditsById.getCast());
                Set<Cast> castSet = this.castService.mapToSet(castDto);
                processMovieCast(castDto, movie, castSet);

                List<CrewApiDTO> crewDto = this.crewService.filterCrewApiDto(creditsById.getCrew());
                Set<Crew> crewSet = this.crewService.mapToSet(crewDto.stream().toList());
                processMovieCrew(crewDto, movie, crewSet);
            }
            page++;
        }

        LocalDateTime end = LocalDateTime.now();
        logger.info("\u001B[32mFinished fetching movies for {}.\u001B[0m", formatDurationLong(Duration.between(start, end)));
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


    private static boolean isInvalid(MovieApiDTO dto) {
        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }

    private void processMovieCast(List<CastApiDTO> castDto, Movie movie, Set<Cast> castSet) {
        this.castService.processCast(
                castDto,
                movie,
                c -> castMovieRepository.findByMovieIdAndCastApiIdAndCharacter(movie.getId(), c.getId(), c.getCharacter()),
                (c, m) -> castService.createCastEntity(
                        c,
                        m,
                        castSet,
                        CastMovie::new,
                        CastMovie::setMovie,
                        CastMovie::setCast,
                        CastMovie::setCharacter
                ),
                castMovieRepository::save
        );
    }

    private void processMovieCrew(List<CrewApiDTO> crewDto, Movie movie, Set<Crew> crewSet) {
        this.crewService.processCrew(
                crewDto,
                movie,
                c -> crewMovieRepository.findByMovieIdAndCrewApiIdAndJobJob(movie.getId(), c.getId(), c.getJob()),
                (c, m) -> {
                    CrewMovie crewMovie = new CrewMovie();
                    crewMovie.setMovie(m);
                    return crewMovie;
                },
                crewMovieRepository::save,
                CrewApiDTO::getJob,
                crewSet
        );
    }

    private MovieResponseApiDTO getMoviesResponseByDateAndVoteCount(int page, int year) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/discover/movie?primary_release_year=%d&sort_by=vote_count.desc&api_key=%s&page=%d",
                year, this.apiConfig.getKey(), page);

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching movies" + "- " + e.getMessage());
            return null;
        }

    }

    private MovieApiByIdResponseDTO getMediaResponseById(Long apiId) {
        String url = String.format(this.apiConfig.getUrl() + "/movie/%d?api_key=" + this.apiConfig.getKey() + "&append_to_response=credits", apiId);
        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(MovieApiByIdResponseDTO.class);
        } catch (Exception e) {
            System.err.println("Error fetching movie with ID: " + apiId + " - " + e.getMessage());
            return null;
        }
    }

    private MovieResponseApiDTO searchQueryApi(String query) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/search/movie?api_key=%s&page=1&query=%s",
                this.apiConfig.getKey(), query);

        try {
            return this.restClient
                    .get()
                    .uri(url)
                    .retrieve()
                    .body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            System.err.println("Error searching movies" + "- " + e.getMessage());
            return null;
        }

    }

    private MoviePageWithGenreDTO mapMoviePageWithGenreDTO(Movie movie) {
        MoviePageWithGenreDTO map = this.modelMapper.map(movie, MoviePageWithGenreDTO.class);
        map.setYear(movie.getReleaseDate().getYear());
        mapOneGenreToPageDTO(map);
        return map;
    }
}
