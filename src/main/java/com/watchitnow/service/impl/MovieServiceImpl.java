package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.MovieApiByIdResponseDTO;
import com.watchitnow.database.model.dto.apiDto.MovieApiDTO;
import com.watchitnow.database.model.dto.apiDto.MovieResponseApiDTO;
import com.watchitnow.database.model.dto.apiDto.TrailerResponseApiDTO;
import com.watchitnow.database.model.dto.detailsDto.MovieDetailsDTO;
import com.watchitnow.database.model.dto.pageDto.MoviePageDTO;
import com.watchitnow.database.model.entity.Movie;
import com.watchitnow.database.model.entity.ProductionCompany;
import com.watchitnow.database.repository.MovieRepository;
import com.watchitnow.service.MovieGenreService;
import com.watchitnow.service.MovieService;
import com.watchitnow.service.ProductionCompanyService;
import com.watchitnow.utils.*;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final MovieGenreService movieGenreService;
    private final ProductionCompanyService productionCompanyService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final ModelMapper modelMapper;
    private final TrailerMappingUtil trailerMappingUtil;
    private final ContentRetrievalUtil contentRetrievalUtil;
    private final MovieMapper movieMapper;
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    public MovieServiceImpl(MovieRepository movieRepository,
                            MovieGenreService movieGenreService,
                            ProductionCompanyService productionCompanyService,
                            ApiConfig apiConfig,
                            RestClient restClient,
                            ModelMapper modelMapper,
                            TrailerMappingUtil trailerMappingUtil,
                            ContentRetrievalUtil contentRetrievalUtil,
                            MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.movieGenreService = movieGenreService;
        this.productionCompanyService = productionCompanyService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.modelMapper = modelMapper;
        this.trailerMappingUtil = trailerMappingUtil;
        this.contentRetrievalUtil = contentRetrievalUtil;
        this.movieMapper = movieMapper;
    }

    @Override
    public Page<MoviePageDTO> getMoviesFromCurrentMonth(Pageable pageable) {
        return contentRetrievalUtil.fetchContentFromDateRange(
                pageable,
                dateRange -> movieRepository.findByReleaseDateBetweenWithGenres(dateRange.start(), dateRange.end()),
                movie -> modelMapper.map(movie, MoviePageDTO.class)
        );
    }


    @Override
    public MovieDetailsDTO getMovieById(long id) {
        return this.modelMapper.map(this.movieRepository.findMovieById(id), MovieDetailsDTO.class);
    }

    @Override
    public Set<MoviePageDTO> getMoviesByGenre(String genreType) {
        return this.movieRepository.findByGenreName(genreType)
                .stream()
                .map(movie -> modelMapper.map(movie, MoviePageDTO.class))
                .collect(Collectors.toSet());
    }

    //    @Scheduled(fixedDelay = 10000)
    //TODO
    private void updateMovies() {
    }

    //    @Scheduled(fixedDelay = 500)
    private void fetchMovies() {
        logger.info("Starting to fetch movies...");

        int year = LocalDate.now().getYear();
        int page = 1;
        int savedMoviesCount = 0;
        int totalPages;

        LocalDate startDate = LocalDate.of(year, 12, 1);

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

        LocalDate endDate = LocalDate.of(year, startDate.getMonthValue(), startDate.lengthOfMonth());

        if (year == 2005) {
            return;
        }

        for (int i = 0; i < 40; i++) {
            logger.info("Fetching page {} of date range {} to {}", page, startDate, endDate);

            MovieResponseApiDTO response = getMovieResponseByDate(page, startDate, endDate);
            totalPages = response.getTotalPages();

            for (MovieApiDTO dto : response.getResults()) {

                if (dto.getPosterPath() == null) {
                    continue;
                }

                if (this.movieRepository.findByApiId(dto.getId()).isEmpty()) {
                    MovieApiByIdResponseDTO responseById = getMovieResponseById(dto.getId());

                    if (responseById == null) {
                        continue;
                    }

                    TrailerResponseApiDTO responseTrailer = trailerMappingUtil.getTrailerResponseById(dto.getId(),
                            this.apiConfig.getUrl(),
                            this.apiConfig.getKey(),
                            "movie");

                    Movie movie = movieMapper.mapToMovie(dto, responseById, responseTrailer);

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
