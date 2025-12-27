package com.moviefy.service.scheduling.evaluation;

import com.moviefy.config.FetchMediaConfig;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiDTO;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieResponseApiDTO;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.service.api.movie.TmdbMoviesEndpointService;
import com.moviefy.service.scheduling.IngestEnum;
import com.moviefy.service.scheduling.persistence.MoviePersistenceWorker;
import com.moviefy.utils.MediaValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.*;


@Service
public class MovieEvaluationOrchestrator {
    private final MovieRepository movieRepository;
    private final TmdbMoviesEndpointService tmdbMoviesEndpointService;
    private final MoviePersistenceWorker moviePersistenceWorker;

    private final Logger logger = LoggerFactory.getLogger(MovieEvaluationOrchestrator.class);

    public MovieEvaluationOrchestrator(MovieRepository movieRepository,
                                       TmdbMoviesEndpointService tmdbMoviesEndpointService,
                                       MoviePersistenceWorker moviePersistenceWorker) {
        this.movieRepository = movieRepository;
        this.tmdbMoviesEndpointService = tmdbMoviesEndpointService;
        this.moviePersistenceWorker = moviePersistenceWorker;
    }

    @Async
    public CompletableFuture<List<Long>> evaluateMovies() {
        logger.info(CYAN + "🎬 Starting MOVIE EVALUATION job (thread={})" + RESET, Thread.currentThread().getName());
        LocalDate today = LocalDate.now();

        List<Long> insertedToday = new ArrayList<>();

        int totalPages = (int) Math.ceil((double) FetchMediaConfig.MAX_MEDIA_PER_YEAR / FetchMediaConfig.API_MEDIA_PER_PAGE) + 10;
        Set<Long> apiIds = new LinkedHashSet<>();

        for (int page = 1; page <= totalPages; page++) {
            MovieResponseApiDTO response = this.tmdbMoviesEndpointService.getMoviesResponseByDateAndVoteCount(page, today.getYear());

            if (response == null || response.getResults() == null) {
                logger.warn(YELLOW + "No results returned for page {}" + RESET, page);
                break;
            }

            if (page > response.getTotalPages()) {
                break;
            }

            for (MovieApiDTO dto : response.getResults()) {
                if (MediaValidationUtil.isInvalid(dto)) {
                    logger.warn(YELLOW + "Invalid movie: {}" + RESET, dto.getId());
                    continue;
                }

                apiIds.add(dto.getId());
            }

            if (apiIds.size() >= FetchMediaConfig.MAX_MEDIA_PER_YEAR) {
                break;
            }
        }

        Set<Long> allApiIdsByApiIdIn = this.movieRepository.findAllApiIdsByApiIdIn(apiIds);
        apiIds.removeAll(allApiIdsByApiIdIn);

        if (apiIds.isEmpty()) {
            logger.info(CYAN + "No new movies to evaluate for year {}" + RESET, today.getYear());
            return CompletableFuture.completedFuture(List.of());
        }

        logger.info(CYAN + "Evaluating {} potential new movies for year {}…" + RESET, apiIds.size(), today.getYear());

        for (Long apiId : apiIds) {
            try {
                IngestEnum result = this.moviePersistenceWorker.persistMovieIfEligible(apiId);

                if (result == IngestEnum.INSERTED) {
                    insertedToday.add(apiId);
                } else if (result == IngestEnum.STOP_EVALUATION) {
                    return CompletableFuture.completedFuture(insertedToday);
                }

            } catch (Exception ex) {
                logger.error(RED + "Failed to evaluate movie apiId={}" + RESET, apiId, ex);
            }
        }
        logger.info(CYAN + "Movie evaluation finished: {} movies inserted/replaced." + RESET, insertedToday.size());
        return CompletableFuture.completedFuture(insertedToday);
    }
}
