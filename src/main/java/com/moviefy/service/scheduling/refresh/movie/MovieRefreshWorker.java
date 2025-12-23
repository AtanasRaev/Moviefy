package com.moviefy.service.scheduling.refresh.movie;

import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.utils.mappers.MediaRefreshMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.moviefy.utils.Ansi.*;

@Service
public class MovieRefreshWorker {
    private final MovieRepository movieRepository;

    private static final Logger logger = LoggerFactory.getLogger(MovieRefreshWorker.class);

    public MovieRefreshWorker(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
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
}
