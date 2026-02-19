package com.moviefy.service.scheduling.refresh.tvSeries;

import com.moviefy.config.cache.RefreshConfig;
import com.moviefy.database.model.dto.apiDto.mediaDto.tvSeriesDto.TvSeriesApiByIdResponseDTO;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.api.tvSeries.TmdbTvEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.BLUE;
import static com.moviefy.utils.Ansi.PURPLE;
import static com.moviefy.utils.Ansi.RED;
import static com.moviefy.utils.Ansi.RESET;
import static com.moviefy.utils.Ansi.YELLOW;

@Service
public class TvSeriesRefreshOrchestrator {
    private final TvSeriesRepository tvSeriesRepository;
    private final TmdbTvEndpointService tmdbTvEndpointService;
    private final TvSeriesRefreshWorker tvSeriesRefreshWorker;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesRefreshOrchestrator.class);

    public TvSeriesRefreshOrchestrator(TvSeriesRepository tvSeriesRepository,
                                       TmdbTvEndpointService tmdbTvEndpointService,
                                       TvSeriesRefreshWorker tvSeriesRefreshWorker) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
        this.tvSeriesRefreshWorker = tvSeriesRefreshWorker;
    }

    @Async
    public CompletableFuture<List<Long>> refreshTvSeries() {
        final String thread = Thread.currentThread().getName();
        final LocalDateTime start = LocalDateTime.now();
        final List<Long> refreshedToday = new ArrayList<>();

        logger.info(BLUE + "Starting TV SERIES REFRESH (thread={})" + RESET, thread);

        LocalDateTime now = LocalDateTime.now();

        List<TvSeries> trending = this.tvSeriesRepository.findAllByPopularityDesc(RefreshConfig.TRENDING_CAP);
        logger.debug(BLUE + "Selected {} trending TV series (limit={})" + RESET,
                trending.size(), RefreshConfig.TRENDING_CAP);

        LocalDateTime startDate = now.minusDays(RefreshConfig.DAYS_CAP);
        LocalDateTime endDate = now.minusDays(RefreshConfig.DAYS_GUARD);

        List<TvSeries> recent = this.tvSeriesRepository.findTvSeriesDueForRefresh(
                startDate, endDate, now.getYear(), now.getYear() - 1, now,
                RefreshConfig.COOL_DOWN_DAYS,
                Math.max(0, RefreshConfig.REFRESH_CAP - trending.size())
        );

        logger.debug(BLUE + "Selected {} recent TV series [{} .. {}), cap={}, remaining={}" + RESET,
                recent.size(), startDate, endDate, RefreshConfig.REFRESH_CAP,
                Math.max(0, RefreshConfig.REFRESH_CAP - trending.size()));

        Set<TvSeries> merged = new LinkedHashSet<>();
        merged.addAll(trending);
        merged.addAll(recent);

        List<Long> apiIdsToRefresh = merged.stream()
                .limit(RefreshConfig.REFRESH_CAP)
                .map(TvSeries::getApiId)
                .toList();

        logger.info(BLUE + "TV refresh queue prepared: {} items (cap={})" + RESET,
                apiIdsToRefresh.size(), RefreshConfig.REFRESH_CAP);

        int attempted = 0;
        int updated = 0;
        int deletedNotFound = 0;
        int nullDto = 0;
        int failed = 0;

        for (Long apiId : apiIdsToRefresh) {
            attempted++;

            try {
                logger.debug(BLUE + "Fetching details for tvApiId={} ({}/{})" + RESET,
                        apiId, attempted, apiIdsToRefresh.size());

                TvSeriesApiByIdResponseDTO dto = this.tmdbTvEndpointService.getTvSeriesResponseById(apiId);

                if (dto == null) {
                    nullDto++;
                    logger.debug(YELLOW + "Skip tvApiId={} — details response is null" + RESET, apiId);
                    continue;
                }

                boolean refreshed = this.tvSeriesRefreshWorker.refreshOneTvSeries(apiId, dto, now);
                if (refreshed) {
                    updated++;
                    refreshedToday.add(apiId);
                    logger.info(PURPLE + "Refreshed series apiId={} ({})" + RESET, apiId, dto.getName());
                } else {
                    logger.debug(YELLOW + "No changes for tvApiId={} - up to date" + RESET, apiId);
                }

            } catch (HttpClientErrorException.NotFound ex) {
                boolean deleted = this.tvSeriesRefreshWorker.deleteMissingTvSeriesByApiId(apiId);
                if (deleted) {
                    deletedNotFound++;
                    logger.warn(YELLOW + "Deleted TV series apiId={} because TMDB returned 404" + RESET, apiId);
                } else {
                    failed++;
                    logger.error(RED + "TMDB returned 404 for tvApiId={}, but delete failed" + RESET, apiId, ex);
                }
            } catch (Exception ex) {
                failed++;
                logger.error(RED + "Failed to refresh tvApiId={}" + RESET, apiId, ex);
            }
        }

        Duration elapsed = Duration.between(start, LocalDateTime.now());
        logger.info(BLUE + "TV series refresh finished: attempted={} updated={} deleted404={} nullDTO={} failed={} took={}ms" + RESET,
                attempted, updated, deletedNotFound, nullDto, failed, elapsed.toMillis());

        return CompletableFuture.completedFuture(refreshedToday);
    }
}
