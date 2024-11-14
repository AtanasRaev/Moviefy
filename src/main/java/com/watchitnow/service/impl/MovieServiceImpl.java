package com.watchitnow.service.impl;

import com.watchitnow.config.ApiConfig;
import com.watchitnow.database.model.dto.apiDto.*;
import com.watchitnow.database.model.dto.detailsDto.MovieDetailsDTO;
import com.watchitnow.database.model.dto.pageDto.MoviePageDTO;
import com.watchitnow.database.model.entity.ProductionCompany;
import com.watchitnow.database.model.entity.credit.Cast.Cast;
import com.watchitnow.database.model.entity.credit.Cast.CastMovie;
import com.watchitnow.database.model.entity.credit.Crew.Crew;
import com.watchitnow.database.model.entity.credit.Crew.CrewMovie;
import com.watchitnow.database.model.entity.credit.Crew.JobCrew;
import com.watchitnow.database.model.entity.media.Movie;
import com.watchitnow.database.repository.CastMovieRepository;
import com.watchitnow.database.repository.CrewMovieRepository;
import com.watchitnow.database.repository.MovieRepository;
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
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class MovieServiceImpl implements MovieService {
    private final MovieRepository movieRepository;
    private final CastMovieRepository castMovieRepository;
    private final CrewMovieRepository crewMovieRepository;
    private final MovieGenreService movieGenreService;
    private final CastService castService;
    private final CrewService crewService;
    private final ProductionCompanyService productionCompanyService;
    private final ApiConfig apiConfig;
    private final RestClient restClient;
    private final ModelMapper modelMapper;
    private final TrailerMappingUtil trailerMappingUtil;
    private final MediaRetrievalUtil mediaRetrievalUtil;
    private final MovieMapper movieMapper;
    private static final Logger logger = LoggerFactory.getLogger(MovieServiceImpl.class);

    public MovieServiceImpl(MovieRepository movieRepository,
                            CastMovieRepository castMovieRepository,
                            CrewMovieRepository crewMovieRepository,
                            MovieGenreService movieGenreService,
                            CastService castService,
                            CrewService crewService,
                            ProductionCompanyService productionCompanyService,
                            ApiConfig apiConfig,
                            RestClient restClient,
                            ModelMapper modelMapper,
                            TrailerMappingUtil trailerMappingUtil,
                            MediaRetrievalUtil mediaRetrievalUtil,
                            MovieMapper movieMapper) {
        this.movieRepository = movieRepository;
        this.castMovieRepository = castMovieRepository;
        this.crewMovieRepository = crewMovieRepository;
        this.movieGenreService = movieGenreService;
        this.castService = castService;
        this.crewService = crewService;
        this.productionCompanyService = productionCompanyService;
        this.apiConfig = apiConfig;
        this.restClient = restClient;
        this.modelMapper = modelMapper;
        this.trailerMappingUtil = trailerMappingUtil;
        this.mediaRetrievalUtil = mediaRetrievalUtil;
        this.movieMapper = movieMapper;
    }

    @Override
    public Page<MoviePageDTO> getMoviesFromCurrentMonth(Pageable pageable) {
        return mediaRetrievalUtil.fetchContentFromDateRange(
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

//    @Scheduled(fixedDelay = 100000)
    //TODO
    private void updateMovies() {
        for (long i = 1; i <= 363912; i++) {
            Optional<Movie> movieOptional = this.movieRepository.findById(i);
            if (movieOptional.isEmpty()) {
                continue;
            }

            Movie movie = movieOptional.get();
            MovieApiByIdResponseDTO dtoMovieById = getMediaResponseById(movie.getApiId());

            if (dtoMovieById == null || movie.getOverview().isBlank()) {
                movie.getGenres().clear();
                movie.getProductionCompanies().clear();
                this.movieRepository.delete(movie);
                continue;
            }

            BigDecimal popularity = BigDecimal.valueOf(dtoMovieById.getPopularity()).setScale(1, RoundingMode.HALF_UP);
            movie.setPopularity(popularity.doubleValue());
            movie.setOriginalTitle(!dtoMovieById.getOriginalTitle().equals(movie.getTitle()) && !dtoMovieById.getOriginalTitle().isBlank()? dtoMovieById.getOriginalTitle() : null);

            MediaResponseCreditsDTO creditsById = getCreditsById(movie.getApiId());
            if (creditsById == null) {
                continue;
            }

            List<CastApiApiDTO> castDto = this.castService.filterCastApiDto(creditsById);
            Set<Cast> castSet = this.castService.mapToSet(castDto, movie);

            castSet.forEach(cast -> {
                CastMovie castMovie = new CastMovie();

                castMovie.setMovie(movie);
                castMovie.setCast(cast);
                String character = castDto.stream().filter(dto -> dto.getId() == cast.getApiId()).toList().get(0).getCharacter();
                castMovie.setCharacter(character == null || character.isBlank() ? null : character);

                castMovieRepository.save(castMovie);
            });


            List<CrewApiApiDTO> crewDto = this.crewService.filterCrewApiDto(creditsById);
            Set<Crew> crewSet = this.crewService.mapToSet(crewDto, movie);

            crewSet.forEach(crew -> {
                CrewMovie crewMovie = new CrewMovie();

                crewMovie.setMovie(movie);
                crewMovie.setCrew(crew);
                String job = crewDto.stream().filter(dto -> dto.getId() == crew.getApiId()).toList().get(0).getJob();

                JobCrew jobByName = this.crewService.findJobByName(job);

                if (jobByName == null) {
                    jobByName = new JobCrew(job);
                    this.crewService.saveJob(jobByName);
                }

                crewMovie.setJob(jobByName);

                this.crewMovieRepository.save(crewMovie);
            });

            this.movieRepository.save(movie);
        }
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

            MovieResponseApiDTO response = getMoviesResponseByDate(page, startDate, endDate);
            totalPages = response.getTotalPages();

            for (MovieApiDTO dto : response.getResults()) {

                if (dto.getPosterPath() == null) {
                    continue;
                }

                if (this.movieRepository.findByApiId(dto.getId()).isEmpty()) {
                    MovieApiByIdResponseDTO responseById = getMediaResponseById(dto.getId());

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
                        //TODO: This and tv-series fetching method cannot be executed at the same time. It may throw exception if they try to save the same production company at the same time
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

    private MovieResponseApiDTO getMoviesResponseByDate(int page, LocalDate startDate, LocalDate endDate) {
        String url = String.format(this.apiConfig.getUrl()
                + "/discover/movie?page=%d&primary_release_date.gte=%s&primary_release_date.lte=%s&api_key="
                + this.apiConfig.getKey(), page, startDate, endDate);

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(MovieResponseApiDTO.class);
    }
    public MovieApiByIdResponseDTO getMediaResponseById(Long apiId) {
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

    public MediaResponseCreditsDTO getCreditsById(Long apiId) {
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
