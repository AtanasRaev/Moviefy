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

    //    @Scheduled(fixedDelay = 100000000)
    //TODO
    private void updateMovies() {
    }

//    @Scheduled(fixedDelay = 500)
    private void fetchMovies() {
        logger.info("Starting to fetch movies...");

        int year = LocalDate.now().getYear();
        int page = 1;
        Long countOldestMovies = this.movieRepository.countOldestMovies();
        int count = countOldestMovies.intValue();

        if (countOldestMovies > 0) {
            year = this.movieRepository.findOldestMovieYear();

            if (countOldestMovies >= 1000) {
                year -= 1;
            } else {
                page = (int) Math.ceil(countOldestMovies / 20.0);
            }
        }

        while (count < 1000) {
            logger.info("Fetching page {} of year {}", page, year);

            MovieResponseApiDTO response = getMoviesResponseByDateAndVoteCount(page, year);

            if (response == null || response.getResults() == null) {
                logger.warn("No results returned for page {} of year {}", page, year);
                break;
            }

            if (page >= response.getTotalPages()) {
                logger.info("Reached the last page for year {}.", year);
                break;
            }

            for (MovieApiDTO dto : response.getResults()) {

                if (this.movieRepository.findByApiId(dto.getId()).isEmpty()) {
                    MovieApiByIdResponseDTO responseById = getMediaResponseById(dto.getId());

                    if (responseById == null || responseById.getRuntime() == null || responseById.getRuntime() < 45) {
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
            }
            page++;
        }

        logger.info("Finished fetching movies.");
    }

    private boolean isEmpty() {
        return this.movieRepository.count() == 0;
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

        return this.restClient
                .get()
                .uri(url)
                .retrieve()
                .body(MovieResponseApiDTO.class);
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
