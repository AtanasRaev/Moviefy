package com.moviefy.service.scheduling.refresh.movie;

import com.moviefy.config.cache.RefreshConfig;
import com.moviefy.database.model.dto.apiDto.mediaDto.movieDto.MovieApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.Movie;
import com.moviefy.database.repository.media.MovieRepository;
import com.moviefy.service.api.movie.TmdbMoviesEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.*;

@Service
public class MovieRefreshService {
    private final MovieRepository movieRepository;
    private final TmdbMoviesEndpointService tmdbMoviesEndpointService;
    private final MovieRefreshItemService movieRefreshItemService;

    private static final Logger logger = LoggerFactory.getLogger(MovieRefreshService.class);

    public MovieRefreshService(MovieRepository movieRepository,
                               TmdbMoviesEndpointService tmdbMoviesEndpointService,
                               MovieRefreshItemService movieRefreshItemService) {
        this.movieRepository = movieRepository;
        this.tmdbMoviesEndpointService = tmdbMoviesEndpointService;
        this.movieRefreshItemService = movieRefreshItemService;
    }

    @Async
    public CompletableFuture<List<Long>> refreshMovies() {
        final String threadName = Thread.currentThread().getName();
        final LocalDateTime start = LocalDateTime.now();
        logger.info(CYAN + "♻️  Starting MOVIE REFRESH (thread={})" + RESET, threadName);

        final List<Long> refreshedToday = new ArrayList<>();
        final LocalDateTime now = LocalDateTime.now();

        List<Movie> allByPopularityDesc = this.movieRepository.findAllByPopularityDesc(RefreshConfig.TRENDING_CAP);
        logger.debug(CYAN + "Selected {} trending candidates (limit={})" + RESET,
                allByPopularityDesc.size(), RefreshConfig.TRENDING_CAP);

        LocalDateTime startDate = now.minusDays(RefreshConfig.DAYS_CAP);
        LocalDateTime endDate = now.minusDays(RefreshConfig.DAYS_GUARD);
        List<Movie> allNewMoviesByDate = this.movieRepository.findAllNewMoviesByDate(
                startDate, endDate, Math.max(0, RefreshConfig.REFRESH_CAP - allByPopularityDesc.size())
        );
        logger.debug(CYAN + "Selected {} recent candidates between [{} .. {}), cap={} (remaining from {})" + RESET,
                allNewMoviesByDate.size(), startDate, endDate, RefreshConfig.REFRESH_CAP,
                Math.max(0, RefreshConfig.REFRESH_CAP - allByPopularityDesc.size()));

        Set<Movie> movies = new LinkedHashSet<>();
        movies.addAll(allByPopularityDesc);
        movies.addAll(allNewMoviesByDate);

        List<Long> apiIdsToRefresh = movies.stream()
                .limit(RefreshConfig.REFRESH_CAP)
                .map(Movie::getApiId)
                .toList();

        logger.info(CYAN + "Refresh queue prepared: {} items (cap={})" + RESET,
                apiIdsToRefresh.size(), RefreshConfig.REFRESH_CAP);

        int attempted = 0;
        int skippedNullDto = 0;
        int updatedCount = 0;
        int failed = 0;

        for (Long apiId : apiIdsToRefresh) {
            attempted++;
            try {
                logger.debug(CYAN + "Fetching details for apiId={} ({}/{})" + RESET, apiId, attempted, apiIdsToRefresh.size());
                MovieApiByIdResponseDTO dto = this.tmdbMoviesEndpointService.getMovieResponseById(apiId);

                if (dto == null) {
                    skippedNullDto++;
                    logger.debug(YELLOW + "Skip apiId={} — details response is null" + RESET, apiId);
                    continue;
                }

                boolean updated = this.movieRefreshItemService.refreshOneMovie(apiId, dto, now);
                if (updated) {
                    updatedCount++;
                    refreshedToday.add(apiId);
                    logger.info(GREEN + "Refreshed movie apiId={} (updated #{})" + RESET, apiId, updatedCount);
                } else {
                    logger.debug(YELLOW + "No changes for apiId={} — up to date" + RESET, apiId);
                }
            } catch (Exception ex) {
                failed++;
                logger.error(RED + "Failed to refresh apiId={}" + RESET, apiId, ex);
            }
        }

        Duration elapsed = Duration.between(start, LocalDateTime.now());
        logger.info(CYAN + "♻️  Movie refresh finished: attempted={} • updated={} • nullDTO={} • failed={} • took={}ms" + RESET,
                attempted, updatedCount, skippedNullDto, failed, elapsed.toMillis());

        return CompletableFuture.completedFuture(refreshedToday);
    }
}
