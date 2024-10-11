package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.*;
import com.watchitnow.database.model.dto.pageDto.MoviePageDTO;
import com.watchitnow.database.model.entity.Movie;
import com.watchitnow.database.model.entity.ProductionCompany;
import com.watchitnow.database.repository.MovieRepository;
import com.watchitnow.service.MovieGenreService;
import com.watchitnow.service.MovieService;
import com.watchitnow.service.ProductionCompanyService;
import com.watchitnow.utils.ContentRetrievalUtil;
import com.watchitnow.utils.DatePaginationUtil;
import com.watchitnow.utils.DateRange;
import com.watchitnow.utils.TrailerMappingUtil;
import org.jetbrains.annotations.Nullable;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final MovieGenreService genreService;
    private final ProductionCompanyService productionCompanyService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final ModelMapper modelMapper;
    private final TrailerMappingUtil trailerMappingUtil;
    private final ContentRetrievalUtil contentRetrievalUtil;
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    public MovieServiceImpl(MovieRepository movieRepository,
                            MovieGenreService genreService,
                            ProductionCompanyService productionCompanyService,
                            ApiConfig apiConfig,
                            RestClient restClient,
                            ModelMapper modelMapper,
                            TrailerMappingUtil trailerMappingUtil,
                            ContentRetrievalUtil contentRetrievalUtil) {
        this.movieRepository = movieRepository;
        this.genreService = genreService;
        this.productionCompanyService = productionCompanyService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.modelMapper = modelMapper;
        this.trailerMappingUtil = trailerMappingUtil;
        this.contentRetrievalUtil = contentRetrievalUtil;
    }

    @Override
    public Set<MoviePageDTO> getMoviesFromCurrentMonth(int targetCount) {
        return contentRetrievalUtil.fetchContentFromDateRange(
                targetCount,
                dateRange -> movieRepository.findByReleaseDateBetweenWithGenres(dateRange.start(), dateRange.end()),
                movie -> modelMapper.map(movie, MoviePageDTO.class),
                MoviePageDTO::getPosterPath
        );
    }

    @Scheduled(fixedDelay = 10000)
    //TODO
    private void updateMovies() {
        long end = 363899;

        for (long i = 1; i <= end; i++) {
            Optional<Movie> movieOptional = this.movieRepository.findById(i);

            if (movieOptional.isEmpty()) {
                continue;
            }

            MovieApiByIdResponseDTO responseById = getMovieResponseById(movieOptional.get().getApiId());

            if (responseById == null) {
                continue;
            }

            TrailerResponseApiDTO responseTrailer = trailerMappingUtil.getTrailerResponseById(movieOptional.get().getApiId(),
                    this.apiConfig.getUrl(),
                    this.apiConfig.getKey(),
                    "movie");

            if (responseTrailer == null) {
                continue;
            }

            BigDecimal popularity = BigDecimal.valueOf(responseById.getPopularity()).setScale(1, RoundingMode.HALF_UP);
            BigDecimal voteAverage = BigDecimal.valueOf(responseById.getVoteAverage()).setScale(1, RoundingMode.HALF_UP);

            movieOptional.get().setPopularity(popularity.doubleValue());
            movieOptional.get().setVoteAverage(voteAverage.doubleValue());
            movieOptional.get().setBackdropPath(responseById.getBackdropPath());

            List<TrailerApiDTO> trailers = responseTrailer.getResults();

            if (trailers.isEmpty()) {
                return;
            }

            TrailerApiDTO selectedTrailer = this.trailerMappingUtil.getTrailer(trailers);

            if (selectedTrailer != null) {
                movieOptional.get().setTrailer(selectedTrailer.getKey());
            }

            this.movieRepository.save(movieOptional.get());
        }
    }

    //    @Scheduled(fixedDelay = 5000000)
    private void fetchMovies() {
        logger.info("Starting to fetch movies...");

        int year = LocalDate.now().getYear();
        int page = 1;
        int savedMoviesCount = 0;
        int totalPages;

        LocalDate startDate = LocalDate.of(year, 12, 1);
        LocalDate endDate = LocalDate.of(year, startDate.getMonthValue(), startDate.lengthOfMonth());

        if (!isEmpty()) {
            List<Movie> oldestMovies = this.movieRepository.findOldestMovie();
            if (!oldestMovies.isEmpty()) {
                Movie oldestMovie = oldestMovies.get(0);

                year = oldestMovie.getReleaseDate().getYear();
                startDate = LocalDate.of(year, oldestMovie.getReleaseDate().getMonthValue(), oldestMovie.getReleaseDate().getDayOfMonth());

                long moviesByYearAndMonth = this.movieRepository.countMoviesInDateRange(oldestMovie.getReleaseDate().getYear(), oldestMovie.getReleaseDate().getMonthValue());

                if (moviesByYearAndMonth > 20) {
                    page = (int) ((moviesByYearAndMonth / 20) + 1);
                }
            }
        }

        if (year == 2005) {
            return;
        }

        for (int i = 0; i < 40; i++) {
            logger.info("Fetching page {} of date range {} to {}", page, startDate, endDate);

            MovieResponseApiDTO response = getMovieResponseByDate(page, startDate, endDate);
            totalPages = response.getTotalPages();

            for (MovieApiDTO dto : response.getResults()) {
                if (this.movieRepository.findByApiId(dto.getId()).isEmpty()) {
                    MovieApiByIdResponseDTO responseById = getMovieResponseById(dto.getId());

                    if (responseById == null) {
                        continue;
                    }

                    Movie movie = mapToMovie(dto, responseById);

                    Map<String, Set<ProductionCompany>> productionCompanyMap = productionCompanyService.getProductionCompaniesFromResponse(responseById, movie);
                    movie.setProductionCompanies(productionCompanyMap.get("all"));

                    if (!productionCompanyMap.get("toSave").isEmpty()) {
                        this.productionCompanyService.saveAllProductionCompanies(productionCompanyMap.get("toSave"));
                    }

                    this.movieRepository.save(movie);

                    savedMoviesCount++;

                    logger.info("Saved movie: {}", movie.getTitle());
                }
            }

            DateRange result = DatePaginationUtil.updatePageAndDate(page, totalPages, i, savedMoviesCount, startDate, endDate, year);
            page = result.getPage();
            startDate = result.getStartDate();
            endDate = result.getEndDate();
            year = result.getYear();
        }

        logger.info("Finished fetching movies.");
    }

    private Movie mapToMovie(MovieApiDTO dto, MovieApiByIdResponseDTO responseById) {
        Movie movie = new Movie();

        movie.setGenres(this.genreService.getAllGenresByApiIds(dto.getGenres()));
        movie.setApiId(dto.getId());
        movie.setTitle(dto.getTitle());
        movie.setOverview(dto.getOverview());
        movie.setPopularity(dto.getPopularity());
        movie.setPosterPath(dto.getPosterPath());
        movie.setReleaseDate(dto.getReleaseDate());
        movie.setRuntime(responseById.getRuntime());
        movie.setVoteAverage(responseById.getVoteAverage());

        return movie;
    }

    private boolean isEmpty() {
        return this.movieRepository.count() == 0;
    }

    private MovieResponseApiDTO getMovieResponseByDate(int page, LocalDate startDate, LocalDate endDate) {
        String url = String.format(this.apiConfig.getUrl()
                + "/discover/movie?page=%d&primary_release_date.gte=%s&primary_release_date.lte=%s&api_key="
                + this.apiConfig.getKey(), page, startDate, endDate);

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(MovieResponseApiDTO.class);
    }

    private MovieApiByIdResponseDTO getMovieResponseById(Long apiId) {
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
}
