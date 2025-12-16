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

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesRefreshService {
    private final TvSeriesRepository tvSeriesRepository;
    private final TmdbTvEndpointService tmdbTvEndpointService;
    private final TvSeriesRefreshItemService tvSeriesRefreshItemService;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesRefreshService.class);

    public TvSeriesRefreshService(TvSeriesRepository tvSeriesRepository,
                                  TmdbTvEndpointService tmdbTvEndpointService,
                                  TvSeriesRefreshItemService tvSeriesRefreshItemService) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
        this.tvSeriesRefreshItemService = tvSeriesRefreshItemService;
    }

    @Async
    public CompletableFuture<List<Long>> refreshTvSeries() {
        final String thread = Thread.currentThread().getName();
        final LocalDateTime start = LocalDateTime.now();
        final List<Long> refreshedToday = new ArrayList<>();

        logger.info(CYAN + "📺 Starting TV SERIES REFRESH (thread={})" + RESET, thread);

        LocalDateTime now = LocalDateTime.now();

        List<TvSeries> trending = this.tvSeriesRepository.findAllByPopularityDesc(RefreshConfig.TRENDING_CAP);
        logger.debug(CYAN + "Selected {} trending TV series (limit={})" + RESET,
                trending.size(), RefreshConfig.TRENDING_CAP);

        LocalDateTime startDate = now.minusDays(RefreshConfig.DAYS_CAP);
        LocalDateTime endDate = now.minusDays(RefreshConfig.DAYS_GUARD);

        List<TvSeries> recent = this.tvSeriesRepository.findAllNewTvSeriesByDate(
                startDate, endDate, Math.max(0, RefreshConfig.REFRESH_CAP - trending.size())
        );

        logger.debug(CYAN + "Selected {} recent TV series [{} .. {}), cap={}, remaining={}" + RESET,
                recent.size(), startDate, endDate, RefreshConfig.REFRESH_CAP,
                Math.max(0, RefreshConfig.REFRESH_CAP - trending.size()));

        Set<TvSeries> merged = new LinkedHashSet<>();
        merged.addAll(trending);
        merged.addAll(recent);

        List<Long> apiIdsToRefresh = merged.stream()
                .limit(RefreshConfig.REFRESH_CAP)
                .map(TvSeries::getApiId)
                .toList();

        logger.info(CYAN + "TV refresh queue prepared: {} items (cap={})" + RESET,
                apiIdsToRefresh.size(), RefreshConfig.REFRESH_CAP);

        int attempted = 0;
        int updated = 0;
        int nullDto = 0;
        int failed = 0;

        for (Long apiId : apiIdsToRefresh) {
            attempted++;

            try {
                logger.debug(CYAN + "Fetching details for tvApiId={} ({}/{})" + RESET,
                        apiId, attempted, apiIdsToRefresh.size());

                TvSeriesApiByIdResponseDTO dto = this.tmdbTvEndpointService.getTvSeriesResponseById(apiId);

                if (dto == null) {
                    nullDto++;
                    logger.debug(YELLOW + "Skip tvApiId={} — details response is null" + RESET, apiId);
                    continue;
                }

                boolean refreshed = this.tvSeriesRefreshItemService.refreshOneTvSeries(apiId, dto, now);
                if (refreshed) {
                    updated++;
                    refreshedToday.add(apiId);
                    logger.info(GREEN + "✔Refreshed series apiId={} ({})" + RESET, apiId, dto.getName());
                } else {
                    logger.debug(YELLOW + "No changes for tvApiId={} — up to date" + RESET, apiId);
                }

            } catch (Exception ex) {
                failed++;
                logger.error(RED + "❌ Failed to refresh tvApiId={}" + RESET, apiId, ex);
            }
        }

        Duration elapsed = Duration.between(start, LocalDateTime.now());
        logger.info(CYAN + "📺 TV series refresh finished: attempted={} • updated={} • nullDTO={} • failed={} • took={}ms" + RESET,
                attempted, updated, nullDto, failed, elapsed.toMillis());

        return CompletableFuture.completedFuture(refreshedToday);
    }
}
