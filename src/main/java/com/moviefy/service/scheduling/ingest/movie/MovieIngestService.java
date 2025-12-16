package com.moviefy.service.scheduling.ingest.movie;

import com.moviefy.config.FetchMediaConfig;
import com.moviefy.database.model.dto.apiDto.mediaDto.MediaResponseCreditsDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.TrailerResponseApiDTO;
import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.media.Collection;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.credit.cast.CastMovieRepository;
import com.moviefy.database.repository.credit.crew.CrewMovieRepository;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.service.api.TmdbCommonEndpointService;
import com.moviefy.service.api.movie.TmdbMoviesEndpointService;
import com.moviefy.service.collection.CollectionService;
import com.moviefy.service.credit.cast.CastService;
import com.moviefy.service.credit.crew.CrewService;
import com.moviefy.service.productionCompanies.ProductionCompanyService;
import com.moviefy.service.scheduling.MediaEventPublisher;
import com.moviefy.utils.EntityComparator;
import com.moviefy.utils.mappers.MovieMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;

import static com.moviefy.utils.Ansi.*;

@Service
public class MovieIngestService {
    private final MovieRepository movieRepository;
    private final CastMovieRepository castMovieRepository;
    private final CrewMovieRepository crewMovieRepository;
    private final TmdbMoviesEndpointService tmdbMoviesEndpointService;
    private final TmdbCommonEndpointService tmdbCommonEndpointService;
    private final CollectionService collectionService;
    private final ProductionCompanyService productionCompanyService;
    private final CastService castService;
    private final CrewService crewService;
    private final MovieMapper movieMapper;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(MovieIngestService.class);

    public MovieIngestService(MovieRepository movieRepository,
                              CastMovieRepository castMovieRepository,
                              CrewMovieRepository crewMovieRepository,
                              TmdbMoviesEndpointService tmdbMoviesEndpointService,
                              TmdbCommonEndpointService tmdbCommonEndpointService,
                              CollectionService collectionService,
                              ProductionCompanyService productionCompanyService,
                              CastService castService,
                              CrewService crewService,
                              MovieMapper movieMapper,
                              MediaEventPublisher mediaEventPublisher) {
        this.movieRepository = movieRepository;
        this.castMovieRepository = castMovieRepository;
        this.crewMovieRepository = crewMovieRepository;
        this.tmdbMoviesEndpointService = tmdbMoviesEndpointService;
        this.tmdbCommonEndpointService = tmdbCommonEndpointService;
        this.collectionService = collectionService;
        this.productionCompanyService = productionCompanyService;
        this.castService = castService;
        this.crewService = crewService;
        this.movieMapper = movieMapper;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean persistMovieIfEligible(MovieApiDTO dto) {
        LocalDate rd = dto.getReleaseDate();
        if (rd == null) {
            logger.debug(YELLOW + "Skip movie: {} — missing release date" + RESET, dto.getTitle());
            return false;
        }

        if (this.movieRepository.findByApiId(dto.getId()).isPresent()) {
            logger.debug(YELLOW + "Skip movie: {} — already exists" + RESET, dto.getTitle());
            return false;
        }

        final int rankingYear = rd.getYear();

        MovieApiByIdResponseDTO responseById = this.tmdbMoviesEndpointService.getMovieResponseById(dto.getId());
        if (responseById == null || responseById.getRuntime() == null || responseById.getRuntime() < FetchMediaConfig.MIN_MOVIE_RUNTIME) {
            logger.debug(YELLOW + "Skip movie: {} — runtime invalid or details missing" + RESET, dto.getTitle());
            return false;
        }

        long countByYear = this.movieRepository.findCountByRankingYear(rankingYear);
        if (countByYear >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
            Optional<Movie> worstOpt = this.movieRepository.findLowestRatedMovieByRankingYear(rankingYear);
            if (worstOpt.isEmpty()) {
                logger.warn(YELLOW + "Skip movie: {} — year {} full and no 'worst' movie found" + RESET,
                        dto.getTitle(), rankingYear);
                return false;
            }

            Movie worst = worstOpt.get();
            if (!EntityComparator.isBetter(dto, worst)) {
                logger.debug(YELLOW + "Skip movie: {} — not better than worst movie {}" + RESET,
                        dto.getTitle(), worst.getTitle());
                return false;
            }

            logger.info(CYAN + "Replacing worst movie '{}' with '{}'" + RESET,
                    worst.getTitle(), dto.getTitle());

            detachAndDelete(worst);
        }

        TrailerResponseApiDTO responseTrailer = this.tmdbCommonEndpointService.getTrailerResponseById(dto.getId(), "movie");

        Movie movie = movieMapper.mapToMovie(responseById, responseTrailer);

        if (responseById.getCollection() != null) {
            Collection collection = this.collectionService.getCollectionFromResponse(responseById.getCollection(), movie);
            movie.setCollection(collection);
        }

        Map<String, Set<ProductionCompany>> pcMap =
                productionCompanyService.getProductionCompaniesFromResponse(responseById, movie);

        movie.setProductionCompanies(pcMap.get("all"));
        if (!pcMap.get("toSave").isEmpty()) {
            this.productionCompanyService.saveAllProduction(pcMap.get("toSave"));
        }

        this.movieRepository.save(movie);

        if (movie.getCollection() != null && movie.getCollection().getName() != null) {
            this.mediaEventPublisher.publishCollectionChangedEvent(movie.getCollection().getName());
            this.mediaEventPublisher.publishMoviesByCollectionChangedEvent(movie.getCollection().getApiId());
            this.mediaEventPublisher.publishMoviesDetailsByCollectionChangedEvent(movie.getCollection().getApiId());
        }

        MediaResponseCreditsDTO credits = responseById.getCredits();
        if (credits != null) {
            this.castService.processMovieCast(credits.getCast(), movie);
            this.crewService.processMovieCrew(credits.getCrew(), movie);

            Set<Long> castIds = this.castMovieRepository.findCastIdsByMovieId(movie.getId());
            this.mediaEventPublisher.publishCastByMovieChangedEvent(castIds);
            this.mediaEventPublisher.publishCastByMediaChangedEvent(castIds);

            Set<Long> crewIds = this.crewMovieRepository.findCrewIdsByMovieId(movie.getId());
            this.mediaEventPublisher.publishCrewByMovieChangedEvent(crewIds);
            this.mediaEventPublisher.publishCrewByMediaChangedEvent(crewIds);
        }

        return true;
    }

    @Transactional
    protected void detachAndDelete(Movie movie) {
        Set<Long> castIds = this.castMovieRepository.findCastIdsByMovieId(movie.getId());
        this.castMovieRepository.deleteByMovieId(movie.getId());
        this.mediaEventPublisher.publishCastByMovieChangedEvent(castIds);
        this.mediaEventPublisher.publishCastByMediaChangedEvent(castIds);

        Set<Long> crewIds = this.crewMovieRepository.findCrewIdsByMovieId(movie.getId());
        this.crewMovieRepository.deleteByMovieId(movie.getId());
        this.mediaEventPublisher.publishCrewByMovieChangedEvent(crewIds);
        this.mediaEventPublisher.publishCrewByMediaChangedEvent(crewIds);

        if (movie.getGenres() != null) {
            movie.getGenres().clear();
        }
        if (movie.getProductionCompanies() != null) {
            movie.getProductionCompanies().clear();
        }

        String removedCollection = null;
        Long collectionApiId = null;

        if (movie.getCollection() != null) {
            removedCollection = movie.getCollection().getName();
            collectionApiId = movie.getCollection().getApiId();
            movie.setCollection(null);
        }

        this.movieRepository.delete(movie);

        if (removedCollection != null) {
            this.mediaEventPublisher.publishCollectionChangedEvent(removedCollection);
        }

        if (collectionApiId != null) {
            this.mediaEventPublisher.publishMoviesByCollectionChangedEvent(collectionApiId);
            this.mediaEventPublisher.publishMoviesDetailsByCollectionChangedEvent(collectionApiId);
        }
    }
}