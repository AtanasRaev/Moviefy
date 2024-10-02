package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.MovieApiDTO;
import com.watchitnow.database.model.dto.apiDto.MovieResponseApiDTO;
import com.watchitnow.database.model.dto.pageDto.MoviePageDTO;
import com.watchitnow.database.model.entity.Movie;
import com.watchitnow.database.repository.MovieRepository;
import com.watchitnow.service.MovieGenreService;
import com.watchitnow.service.MovieService;
import com.watchitnow.utils.ContentRetrievalUtil;
import com.watchitnow.utils.DatePaginationUtil;
import com.watchitnow.utils.DateRange;
import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final MovieGenreService genreService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final ModelMapper modelMapper;
    private final ContentRetrievalUtil contentRetrievalUtil;
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    public MovieServiceImpl(MovieRepository movieRepository,
                            MovieGenreService genreService,
                            ApiConfig apiConfig,
                            RestClient restClient,
                            ModelMapper modelMapper,
                            ContentRetrievalUtil contentRetrievalUtil) {
        this.movieRepository = movieRepository;
        this.genreService = genreService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.modelMapper = modelMapper;
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

    //    @Scheduled(fixedDelay = 5000)
    private void fetchMovies() {
        logger.info("Starting to fetch movies...");
        int year = LocalDate.now().getYear();
        LocalDate startDate = LocalDate.of(year, 12, 1);
        int page = 1;

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

        int totalPages;
        LocalDate endDate = LocalDate.of(year, startDate.getMonthValue(), startDate.lengthOfMonth());

        int savedMoviesCount = 0;

        for (int i = 0; i < 40; i++) {
            logger.info("Fetching page {} of date range {} to {}", page, startDate, endDate);

            MovieResponseApiDTO response = getResponse(page, startDate, endDate);
            totalPages = response.getTotalPages();

            for (MovieApiDTO dto : response.getResults()) {
                if (this.movieRepository.findByApiId(dto.getId()).isEmpty()) {
                    Movie movie = new Movie();

                    movie.setGenres(this.genreService.getAllGenresByApiIds(dto.getGenres()));
                    movie.setApiId(dto.getId());
                    movie.setTitle(dto.getTitle());
                    movie.setOverview(dto.getOverview());
                    movie.setPopularity(dto.getPopularity());
                    movie.setPosterPath(dto.getPosterPath());
                    movie.setReleaseDate(dto.getReleaseDate());

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

    private MovieResponseApiDTO getResponse(int page, LocalDate startDate, LocalDate endDate) {
        String url = String.format(this.apiConfig.getUrl()
                        + "/discover/movie?page=%d&primary_release_date.gte=%s&primary_release_date.lte=%s&api_key="
                        + this.apiConfig.getKey()
                , page, startDate, endDate);

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(MovieResponseApiDTO.class);
    }
}
