package com.moviefy.service.ingest.tvSeries;

import com.moviefy.database.model.dto.apiDto.TvSeriesApiDTO;
import com.moviefy.database.model.dto.apiDto.TvSeriesResponseApiDTO;
import com.moviefy.database.repository.media.tvSeries.TvSeriesRepository;
import com.moviefy.service.api.tvSeries.TmdbTvEndpointService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.moviefy.utils.Ansi.*;

@Service
public class TvSeriesIngestJob {
    private final TvSeriesRepository tvSeriesRepository;
    private final TvSeriesIngestService tvSeriesIngestService;
    private final TmdbTvEndpointService tmdbTvEndpointService;

    private static final Logger logger = LoggerFactory.getLogger(TvSeriesIngestJob.class);
    private static final int DAILY_INSERT_LIMIT = 10;

    public TvSeriesIngestJob(TvSeriesRepository tvSeriesRepository,
                             TvSeriesIngestService tvSeriesIngestService,
                             TmdbTvEndpointService tmdbTvEndpointService) {
        this.tvSeriesRepository = tvSeriesRepository;
        this.tvSeriesIngestService = tvSeriesIngestService;
        this.tmdbTvEndpointService = tmdbTvEndpointService;
    }

    @Transactional
    @Async
    public CompletableFuture<Integer> addNewSeries() {
        final long startNs = System.nanoTime();

        logger.info(BLUE + "📺 Starting TV SERIES ingest job… (thread={})" + RESET,
                Thread.currentThread().getName());

        int page = 1;
        int insertedToday = 0;

        while (insertedToday < DAILY_INSERT_LIMIT) {
            logger.debug(BLUE + "Fetching series (page={})…" + RESET, page);

            TvSeriesResponseApiDTO discover = this.tmdbTvEndpointService.getSeriesFromToday(page);
            TvSeriesResponseApiDTO trending = this.tmdbTvEndpointService.getTrendingSeries(page);

            Map<Long, TvSeriesApiDTO> byId = new LinkedHashMap<>();
            if (trending != null && trending.getResults() != null) {
                trending.getResults().forEach(r -> byId.put(r.getId(), r));
            }
            if (discover != null && discover.getResults() != null) {
                discover.getResults().forEach(r -> byId.put(r.getId(), r));
            }

            if (byId.isEmpty()) {
                logger.debug(YELLOW + "Page {} returned no results. Ending series ingest." + RESET, page);
                break;
            }

            Collection<TvSeriesApiDTO> candidates = byId.values();
            Set<Long> trendingIds = buildTrendingIdSet(trending);
            final LocalDate now = LocalDate.now();

            Set<TvSeriesApiDTO> filtered = candidates.stream()
                    .filter(dto -> {
                        if (isInvalid(dto) || dto.getFirstAirDate() == null) return false;
                        long days = ChronoUnit.DAYS.between(dto.getFirstAirDate(), now);
                        if (days <= 7)  return dto.getPopularity() >= 5 || trendingIds.contains(dto.getId());
                        if (days <= 30) return dto.getVoteCount() >= 5 || dto.getPopularity() >= 10;
                        return dto.getVoteCount() >= 20 || dto.getPopularity() >= 20;
                    })
                    .collect(Collectors.toSet());

            logger.debug(BLUE + "Page {}: {} candidates → {} filtered" + RESET,
                    page, candidates.size(), filtered.size());

            if (filtered.isEmpty()) {
                page++;
                continue;
            }

            Set<Long> incomingIds = filtered.stream().map(TvSeriesApiDTO::getId).collect(Collectors.toSet());
            Set<Long> existing = this.tvSeriesRepository.findIdsAllByApiIdIn(incomingIds);

            List<TvSeriesApiDTO> newOnes = filtered.stream()
                    .filter(d -> !existing.contains(d.getId()))
                    .toList();

            logger.debug(BLUE + "Page {}: {} new series after removing existing." + RESET, page, newOnes.size());

            if (newOnes.isEmpty()) {
                page++;
                continue;
            }

            List<TvSeriesApiDTO> queue = newOnes.stream()
                    .sorted(Comparator
                            .comparing(TvSeriesApiDTO::getVoteCount, Comparator.nullsFirst(Comparator.reverseOrder()))
                            .thenComparing(TvSeriesApiDTO::getPopularity, Comparator.nullsFirst(Comparator.reverseOrder()))
                            .thenComparing(TvSeriesApiDTO::getId))
                    .toList();

            for (TvSeriesApiDTO dto : queue) {
                if (insertedToday >= DAILY_INSERT_LIMIT) {
                    logger.debug(YELLOW + "Reached daily insert limit ({}) . Stopping series ingest." + RESET, DAILY_INSERT_LIMIT);
                    break;
                }

                boolean inserted = this.tvSeriesIngestService.persistSeriesIfEligible(dto);

                if (inserted) {
                    insertedToday++;
                    logger.info(PURPLE + "Inserted series: {} (#{}/{} • voteCount={} • popularity={})" + RESET,
                            dto.getName(), insertedToday, DAILY_INSERT_LIMIT, dto.getVoteCount(), dto.getPopularity());
                } else {
                    logger.debug(YELLOW + "Skipped series: {} (ID={})" + RESET, dto.getName(), dto.getId());
                }
            }

            page++;
        }

        long tookMs = (System.nanoTime() - startNs) / 1_000_000L;
        logger.info(BLUE + "📺 TV SERIES ingest finished: inserted={} • took={} ms" + RESET, insertedToday, tookMs);

        return CompletableFuture.completedFuture(insertedToday);
    }

    private static Set<Long> buildTrendingIdSet(TvSeriesResponseApiDTO trending) {
        if (trending == null || trending.getResults() == null) return Collections.emptySet();
        return trending.getResults().stream().map(TvSeriesApiDTO::getId).collect(Collectors.toSet());
    }

    private static boolean isInvalid(TvSeriesApiDTO dto) {
        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || dto.getName() == null || dto.getName().isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }
}
