package com.moviefy.service.scheduling.ingest;

import com.moviefy.config.cache.IngestConfig;
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
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static com.moviefy.utils.Ansi.*;

@Service
public class MovieIngestOrchestrator {
    private final MovieRepository movieRepository;
    private final TmdbMoviesEndpointService tmdbMoviesEndpointService;
    private final MoviePersistenceWorker moviePersistenceWorker;

    private static final Logger logger = LoggerFactory.getLogger(MovieIngestOrchestrator.class);

    public MovieIngestOrchestrator(MovieRepository movieRepository,
                                   TmdbMoviesEndpointService tmdbMoviesEndpointService,
                                   MoviePersistenceWorker moviePersistenceWorker) {
        this.movieRepository = movieRepository;
        this.tmdbMoviesEndpointService = tmdbMoviesEndpointService;
        this.moviePersistenceWorker = moviePersistenceWorker;
    }

    @Async
    public CompletableFuture<List<Long>> addNewMovies() {
        logger.info(CYAN + "🎬 Starting MOVIE INGEST job (thread={})" + RESET, Thread.currentThread().getName());

        int page = 1;
        List<Long> insertedToday = new ArrayList<>();

        while (insertedToday.size() < IngestConfig.DAILY_INSERT_LIMIT) {
            if (page >= 4) {
                return CompletableFuture.completedFuture(insertedToday);
            }

            logger.debug(CYAN + "Fetching movies (page={})…" + RESET, page);

            MovieResponseApiDTO discover = this.tmdbMoviesEndpointService.getNewMoviesUTCTime(page);
            MovieResponseApiDTO trending = this.tmdbMoviesEndpointService.getTrendingMovies(page);

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
            final LocalDate now = LocalDate.now();

            Set<MovieApiDTO> filtered = candidates.stream()
                    .filter(dto -> MediaValidationUtil.isValidForUpdate(dto, now, trendingIds))
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
                if (insertedToday.size() >= IngestConfig.DAILY_INSERT_LIMIT) {
                    logger.debug(CYAN + "Reached daily insert limit ({}). Stopping." + RESET, IngestConfig.DAILY_INSERT_LIMIT);
                    break;
                }

                try {
                    IngestEnum result = moviePersistenceWorker.persistMovieIfEligible(dto.getId());
                    if (result == IngestEnum.INSERTED) {
                        insertedToday.add(dto.getId());
                        logger.info(GREEN + "Inserted movie: {} (#{}/{} • voteCount={} • popularity={})" + RESET,
                                dto.getTitle(), insertedToday, IngestConfig.DAILY_INSERT_LIMIT, dto.getVoteCount(), dto.getPopularity());
                    } else {
                        logger.debug(YELLOW + "Skipped movie: {} (ID={})" + RESET, dto.getTitle(), dto.getId());
                    }

                } catch (Exception ex) {
                    logger.error(RED + "Failed to ingest movie: {} (ID={})" + RESET,
                            dto.getTitle(), dto.getId(), ex);
                }
            }
            page++;
        }

        logger.info(CYAN + "🎬 Movie ingest finished: inserted={}" + RESET, insertedToday);
        return CompletableFuture.completedFuture(insertedToday);
    }

    private static Set<Long> buildTrendingIdSet(MovieResponseApiDTO trending) {
        if (trending == null || trending.getResults() == null) return Collections.emptySet();
        return trending.getResults().stream().map(MovieApiDTO::getId).collect(Collectors.toSet());
    }
}
