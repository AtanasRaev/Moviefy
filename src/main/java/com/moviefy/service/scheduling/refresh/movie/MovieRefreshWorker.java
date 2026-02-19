package com.moviefy.service.scheduling.refresh.movie;

import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.credit.cast.CastMovieRepository;
import com.moviefy.database.repository.credit.crew.CrewMovieRepository;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.service.scheduling.MediaEventPublisher;
import com.moviefy.utils.mappers.MediaRefreshMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

import static com.moviefy.utils.Ansi.*;

@Service
public class MovieRefreshWorker {
    private final MovieRepository movieRepository;
    private final CastMovieRepository castMovieRepository;
    private final CrewMovieRepository crewMovieRepository;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(MovieRefreshWorker.class);

    public MovieRefreshWorker(MovieRepository movieRepository,
                              CastMovieRepository castMovieRepository,
                              CrewMovieRepository crewMovieRepository,
                              MediaEventPublisher mediaEventPublisher) {
        this.movieRepository = movieRepository;
        this.castMovieRepository = castMovieRepository;
        this.crewMovieRepository = crewMovieRepository;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean refreshOneMovie(Long apiId, MovieApiByIdResponseDTO dto, LocalDateTime now) {

        logger.debug(CYAN + "Refreshing movie apiId={} (tx=REQUIRES_NEW)" + RESET, apiId);

        Optional<Movie> opt = this.movieRepository.findByApiId(apiId);
        if (opt.isEmpty()) {
            logger.warn(YELLOW + "Skip refresh: movie apiId={} not found in database" + RESET, apiId);
            return false;
        }

        Movie movie = opt.get();

        boolean updated;
        try {
            updated = MediaRefreshMapper.mapCommonFields(movie, dto, now);
        } catch (Exception ex) {
            logger.error(RED + "❌ Failed to map refresh fields for apiId={}" + RESET, apiId, ex);
            return false;
        }

        if (!updated) {
            logger.debug(YELLOW + "No changes detected for apiId={} — movie already up to date" + RESET, apiId);
            return false;
        }

        try {
            this.movieRepository.save(movie);
        } catch (Exception ex) {
            logger.error(RED + "❌ Failed to save refreshed movie apiId={}" + RESET, apiId, ex);
            return false;
        }
        return true;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean deleteMissingMovieByApiId(Long apiId) {
        Optional<Movie> opt = this.movieRepository.findByApiId(apiId);
        if (opt.isEmpty()) {
            logger.warn(YELLOW + "Skip delete: movie apiId={} not found in database" + RESET, apiId);
            return false;
        }

        Movie movie = opt.get();

        try {
            Set<Long> castIds = this.castMovieRepository.findCastIdsByMovieId(movie.getId());
            this.castMovieRepository.deleteByMovieId(movie.getId());
            if (!castIds.isEmpty()) {
                this.mediaEventPublisher.publishCastByMovieChangedEvent(castIds);
                this.mediaEventPublisher.publishCastByMediaChangedEvent(castIds);
            }

            Set<Long> crewIds = this.crewMovieRepository.findCrewIdsByMovieId(movie.getId());
            this.crewMovieRepository.deleteByMovieId(movie.getId());
            if (!crewIds.isEmpty()) {
                this.mediaEventPublisher.publishCrewByMovieChangedEvent(crewIds);
                this.mediaEventPublisher.publishCrewByMediaChangedEvent(crewIds);
            }

            this.movieRepository.deleteFavoritesByMovieId(movie.getId());

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

            return true;
        } catch (Exception ex) {
            logger.error(RED + "Failed to delete missing movie apiId={}" + RESET, apiId, ex);
            return false;
        }
    }
}
