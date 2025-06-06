package com.moviefy.service.impl;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.*;
import com.moviefy.database.model.dto.detailsDto.MovieDetailsDTO;
import com.moviefy.database.model.dto.detailsDto.MovieDetailsHomeDTO;
import com.moviefy.database.model.dto.pageDto.CrewHomePageDTO;
import com.moviefy.database.model.dto.pageDto.CrewPageDTO;
import com.moviefy.database.model.dto.pageDto.GenrePageDTO;
import com.moviefy.database.model.dto.pageDto.ProductionHomePageDTO;
import com.moviefy.database.model.dto.pageDto.movieDto.*;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.credit.cast.Cast;
import com.moviefy.database.model.entity.credit.cast.CastMovie;
import com.moviefy.database.model.entity.credit.crew.Crew;
import com.moviefy.database.model.entity.credit.crew.CrewMovie;
import com.moviefy.database.model.entity.genre.MovieGenre;
import com.moviefy.database.model.entity.media.Collection;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.CastMovieRepository;
import com.moviefy.database.repository.CrewMovieRepository;
import com.moviefy.database.repository.MovieRepository;
import com.moviefy.service.*;
import com.moviefy.utils.MovieMapper;
import com.moviefy.utils.TrailerMappingUtil;
import jakarta.transaction.Transactional;
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
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);
    private static final int START_YEAR = 1970;
    private static final int MAX_MOVIES_PER_YEAR = 1200;
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
                            MovieMapper movieMapper) {
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
    }

    @Override
    public Page<MoviePageDTO> getMoviesFromCurrentMonth(Pageable pageable) {
        return movieRepository.findByReleaseDate(
                getStartOfCurrentMonth(),
                pageable
        ).map(movie -> {
            MoviePageDTO map = modelMapper.map(movie, MoviePageDTO.class);
            map.setYear(movie.getReleaseDate().getYear());
            return map;
        });
    }

    @Override
    public MovieDetailsDTO getMovieDetailsById(Long id) {
        return this.movieRepository.findMovieById(id)
                .map(this::mapToMovieDetailsDTO)
                .orElse(null);
    }

    @Override
    public Set<MoviePageDTO> getMoviesByGenre(String genreType) {
        return this.movieRepository.findByGenreName(genreType)
                .stream()
                .map(movie -> modelMapper.map(movie, MoviePageDTO.class))
                .collect(Collectors.toSet());
    }

    @Override
    public Page<MoviePageWithGenreDTO> getTrendingMovies(Pageable pageable) {
        return this.movieRepository.findAllByPopularityDesc(pageable)
                .map(movie -> {
                    MoviePageWithGenreDTO map = modelMapper.map(movie, MoviePageWithGenreDTO.class);
                    mapOneGenreToPageDTO(map);
                    return map;
                });
    }

    private void mapOneGenreToPageDTO(MoviePageWithGenreDTO map) {
        Optional<MovieGenre> optional = this.movieGenreService.getAllGenresByMovieId(map.getId()).stream().findFirst();
        optional.ifPresent(genre -> map.setGenre(genre.getName()));
    }

    @Override
    public Page<MoviePageWithGenreDTO> getPopularMovies(Pageable pageable) {
        return this.movieRepository.findAllSortedByVoteCount(pageable)
                .map(movie -> {
                    MoviePageWithGenreDTO map = this.modelMapper.map(movie, MoviePageWithGenreDTO.class);
                    mapOneGenreToPageDTO(map);
                    return map;
                });
    }

    @Override
    public boolean isEmpty() {
        return this.movieRepository.count() == 0;
    }

    @Override
    public MovieDetailsHomeDTO getFirstMovieByCollectionName(String name) {
        List<Collection> collections = collectionService.getByName(name);

        if (collections.isEmpty()) {
            return null;
        }

        Collection collection = collections.get(0);

        return collection.getMovies()
                .stream()
                .min(Comparator.comparing(Movie::getReleaseDate))
                .map(this::mapToMovieDetailsHomeDTO)
                .orElse(null);
    }

    @Override
    public List<MovieHomeDTO> getMoviesByCollectionName(String name) {
        List<Collection> byName = this.collectionService.getByName(name);

        if (byName.isEmpty()) {
            return List.of();
        }

        Collection collection = byName.get(0);

        return collection.getMovies()
                .stream()
                .sorted(Comparator.comparing(Movie::getReleaseDate))
                .map(movie -> this.modelMapper.map(movie, MovieHomeDTO.class))
                .toList();
    }

    @Override
    public List<CollectionPageDTO> getCollectionsByName(List<String> input) {
        List<Collection> byName = this.collectionService.getByNames(input);

        if (byName.isEmpty()) {
            return List.of();
        }

        return byName.stream()
                .map(c -> {
                    CollectionPageDTO map = this.modelMapper.map(c, CollectionPageDTO.class);
                    c.getMovies()
                            .stream()
                            .min(Comparator.comparing(Movie::getId))
                            .ifPresent(movie -> {
                                map.setFirstMovieId(movie.getId());
                                map.setOverview(movie.getOverview());
                                map.setRuntime(movie.getRuntime());
                            });
                    map.setVoteAverage(c.getMovies()
                            .stream()
                            .mapToDouble(Movie::getVoteAverage)
                            .average()
                            .orElse(0));
                    return map;
                })
                .toList();
    }

    @Override
    public Page<MoviePageWithGenreDTO> searchMovies(String query, Pageable pageable) {
        return this.movieRepository.searchByTitle(query, pageable)
                .map(movie -> {
                    MoviePageWithGenreDTO map = this.modelMapper.map(movie, MoviePageWithGenreDTO.class);
                    mapOneGenreToPageDTO(map);
                    return map;
                });
    }


    //    @Scheduled(fixedDelay = 100000000)
    //TODO
    private void updateMovies() {
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
                    return map;
                })
                .toList();
    }

    private MovieDetailsHomeDTO mapToMovieDetailsHomeDTO(Movie movie) {
        MovieDetailsHomeDTO movieDetails = modelMapper.map(movie, MovieDetailsHomeDTO.class);

        List<ProductionHomePageDTO> productionCompanies = getProductionCompanies(movie);
        List<CrewHomePageDTO> crew = getCrewForMovie(movie);

        movieDetails.setProductionCompany(productionCompanies);
        movieDetails.setCrew(crew);

        return movieDetails;
    }

    private List<ProductionHomePageDTO> getProductionCompanies(Movie movie) {
        return movieRepository.findMovieById(movie.getId())
                .map(foundMovie -> foundMovie.getProductionCompanies()
                        .stream()
                        .sorted(Comparator.comparing(ProductionCompany::getId))
                        .map(company -> modelMapper.map(company, ProductionHomePageDTO.class))
                        .toList())
                .orElse(List.of());
    }

    private List<CrewHomePageDTO> getCrewForMovie(Movie movie) {
        return crewService.getCrewByMediaId("movie", movie.getId())
                .stream()
                .sorted(Comparator.comparing(CrewPageDTO::getId))
                .map(crew -> modelMapper.map(crew, CrewHomePageDTO.class))
                .toList();
    }

    private void fetchMovies() {
        logger.info("Starting to fetch movies...");

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
            logger.info("Movie - Fetching page {} of year {}", page, year);

            MovieResponseApiDTO response = getMoviesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn("No results returned for page {} of year {}", page, year);
                break;
            }

            if (page > response.getTotalPages()) {
                logger.info("Reached the last page for year {}.", year);
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
                    logger.warn("Invalid movie: {}", dto.getId());
                    continue;
                }

                if (this.movieRepository.findByApiId(dto.getId()).isPresent()) {
                    logger.info("Movie already exists: {}", dto.getId());
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

                logger.info("Saved movie: {}", movie.getTitle());

                MediaResponseCreditsDTO creditsById = getCreditsById(dto.getId());

                if (creditsById == null) {
                    continue;
                }

                List<CastApiApiDTO> castDto = this.castService.filterCastApiDto(creditsById);
                Set<Cast> castSet = this.castService.mapToSet(castDto);
                processMovieCast(castDto, movie, castSet);

                List<CrewApiDTO> crewDto = this.crewService.filterCrewApiDto(creditsById);
                Set<Crew> crewSet = this.crewService.mapToSet(crewDto.stream().toList());
                processMovieCrew(crewDto, movie, crewSet);
            }
            page++;
        }

        logger.info("Finished fetching movies.");
    }

    private static boolean isInvalid(MovieApiDTO dto) {
        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }

    private void processMovieCast(List<CastApiApiDTO> castDto, Movie movie, Set<Cast> castSet) {
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
        String url = String.format(this.apiConfig.getUrl() + "/movie/%d?api_key=" + this.apiConfig.getKey(), apiId);
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

    private MediaResponseCreditsDTO getCreditsById(Long apiId) {
        String url = String.format(this.apiConfig.getUrl() + "/movie/%d/credits?api_key=%s", apiId, this.apiConfig.getKey());

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
