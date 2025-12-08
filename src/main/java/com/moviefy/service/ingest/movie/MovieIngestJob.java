package com.moviefy.service.ingest.movie;

import com.moviefy.config.ApiConfig;
import com.moviefy.database.model.dto.apiDto.MovieApiDTO;
import com.moviefy.database.model.dto.apiDto.MovieResponseApiDTO;
import com.moviefy.database.repository.media.MovieRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.moviefy.utils.Ansi.*;

@Service
public class MovieIngestJob {
    private final RestClient restClient;
    private final ApiConfig apiConfig;
    private final MovieRepository movieRepository;
    private final MovieIngestService movieIngestService;

    private static final Logger logger = LoggerFactory.getLogger(MovieIngestJob.class);
    private static final ZoneId SOFIA_TZ = ZoneId.of("Europe/Sofia");
    private static final int DAILY_INSERT_LIMIT = 10;

    public MovieIngestJob(RestClient restClient,
                          ApiConfig apiConfig,
                          MovieRepository movieRepository,
                          MovieIngestService movieIngestService) {
        this.restClient = restClient;
        this.apiConfig = apiConfig;
        this.movieRepository = movieRepository;
        this.movieIngestService = movieIngestService;
    }

    @Transactional
    @Async
    public CompletableFuture<Integer> addNewMovies() {
        final long startNs = System.nanoTime();

        logger.info(CYAN + "🎬 Starting MOVIE INGEST job (thread={})" + RESET, Thread.currentThread().getName());

        int page = 1;
        int insertedToday = 0;

        while (insertedToday < DAILY_INSERT_LIMIT) {
            logger.debug(CYAN + "Fetching movies (page={})…" + RESET, page);

            MovieResponseApiDTO discover = getMoviesFromToday(page);
            MovieResponseApiDTO trending = getTrendingMovies(page);

            Map<Long, MovieApiDTO> byId = new LinkedHashMap<>();
            if (trending != null && trending.getResults() != null) {
                trending.getResults().forEach(r -> byId.put(r.getId(), r));
            }
            if (discover != null && discover.getResults() != null) {
                discover.getResults().forEach(r -> byId.put(r.getId(), r));
            }

            if (byId.isEmpty()) {
                logger.debug(CYAN + "Page {} returned no results. Ending ingest." + RESET, page);
                break;
            }

            Collection<MovieApiDTO> candidates = byId.values();
            Set<Long> trendingIds = buildTrendingIdSet(trending);
            final LocalDate now = LocalDate.now(SOFIA_TZ);

            Set<MovieApiDTO> filtered = candidates.stream()
                    .filter(dto -> {
                        if (isInvalid(dto) || dto.getReleaseDate() == null) return false;
                        long days = ChronoUnit.DAYS.between(dto.getReleaseDate(), now);
                        if (days <= 7)  return dto.getPopularity() >= 5 || trendingIds.contains(dto.getId());
                        if (days <= 30) return dto.getVoteCount() >= 5 || dto.getPopularity() >= 10;
                        return dto.getVoteCount() >= 20 || dto.getPopularity() >= 20;
                    })
                    .collect(Collectors.toSet());

            logger.debug(CYAN + "Page {}: {} candidates → {} filtered" + RESET,
                    page, candidates.size(), filtered.size());

            if (filtered.isEmpty()) {
                page++;
                continue;
            }

            Set<Long> incomingIds = filtered.stream().map(MovieApiDTO::getId).collect(Collectors.toSet());
            Set<Long> existing = movieRepository.findIdsAllByApiIdIn(incomingIds);

            List<MovieApiDTO> newOnes = filtered.stream()
                    .filter(d -> !existing.contains(d.getId()))
                    .toList();

            logger.debug(CYAN + "Page {}: {} new movies after removing existing." + RESET, page, newOnes.size());

            if (newOnes.isEmpty()) {
                page++;
                continue;
            }

            List<MovieApiDTO> queue = newOnes.stream()
                    .sorted(Comparator
                            .comparing(MovieApiDTO::getVoteCount, Comparator.nullsFirst(Comparator.reverseOrder()))
                            .thenComparing(MovieApiDTO::getPopularity, Comparator.nullsFirst(Comparator.reverseOrder()))
                            .thenComparing(MovieApiDTO::getId))
                    .toList();

            for (MovieApiDTO dto : queue) {
                if (insertedToday >= DAILY_INSERT_LIMIT) {
                    logger.debug(CYAN + "Reached daily insert limit ({}). Stopping." + RESET, DAILY_INSERT_LIMIT);
                    break;
                }

                boolean inserted = movieIngestService.persistMovieIfEligible(dto);
                if (inserted) {
                    insertedToday++;
                    logger.info(GREEN + "Inserted movie: {} (#{}/{} • voteCount={} • popularity={})" + RESET,
                            dto.getTitle(), insertedToday, DAILY_INSERT_LIMIT, dto.getVoteCount(), dto.getPopularity());
                } else {
                    logger.debug(YELLOW + "Skipped movie: {} (ID={})" + RESET, dto.getTitle(), dto.getId());
                }
            }

            page++;
        }

        long tookMs = (System.nanoTime() - startNs) / 1_000_000L;
        logger.info(CYAN + "🎬 Movie ingest finished: inserted={} • took={} ms" + RESET, insertedToday, tookMs);

        return CompletableFuture.completedFuture(insertedToday);
    }

    private MovieResponseApiDTO getMoviesFromToday(int page) {
        LocalDate today = LocalDate.now(SOFIA_TZ);
        LocalDate yesterday = today.minusDays(1);

        String fromDate = yesterday.format(DateTimeFormatter.ISO_DATE);
        String toDate   = today.format(DateTimeFormatter.ISO_DATE);

        String url = String.format(
                "%s/discover/movie?primary_release_date.gte=%s&primary_release_date.lte=%s&sort_by=popularity.desc&api_key=%s&page=%d",
                apiConfig.getUrl(),
                fromDate,
                toDate,
                apiConfig.getKey(),
                page
        );

        try {
            return restClient.get().uri(url).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.warn("Error fetching discover movies: {}", e.getMessage());
            return null;
        }
    }

    private MovieResponseApiDTO getTrendingMovies(int page) {
        String url = String.format("%s/trending/movie/day?api_key=%s&page=%d",
                apiConfig.getUrl(), apiConfig.getKey(), page);

        try {
            return restClient.get().uri(url).retrieve().body(MovieResponseApiDTO.class);
        } catch (Exception e) {
            logger.warn("Error fetching trending movies: {}", e.getMessage());
            return null;
        }
    }

    private static Set<Long> buildTrendingIdSet(MovieResponseApiDTO trending) {
        if (trending == null || trending.getResults() == null) return Collections.emptySet();
        return trending.getResults().stream().map(MovieApiDTO::getId).collect(Collectors.toSet());
    }

    private static boolean isInvalid(MovieApiDTO dto) {
        return dto.getPosterPath() == null || dto.getPosterPath().isBlank()
                || dto.getOverview() == null || dto.getOverview().isBlank()
                || dto.getTitle() == null || dto.getTitle().isBlank()
                || dto.getBackdropPath() == null || dto.getBackdropPath().isBlank();
    }
}
