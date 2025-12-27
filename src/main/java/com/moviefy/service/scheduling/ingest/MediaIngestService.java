package com.moviefy.service.scheduling.ingest;

import com.moviefy.service.scheduling.MediaEventPublisher;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.*;

@Service
public class MediaIngestService {
    private final MovieIngestOrchestrator movieIngestOrchestrator;
    private final TvSeriesIngestOrchestrator tvSeriesIngestOrchestrator;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(MediaIngestService.class);


    public MediaIngestService(MovieIngestOrchestrator movieIngestOrchestrator,
                              TvSeriesIngestOrchestrator tvSeriesIngestOrchestrator,
                              MediaEventPublisher mediaEventPublisher) {
        this.movieIngestOrchestrator = movieIngestOrchestrator;
        this.tvSeriesIngestOrchestrator = tvSeriesIngestOrchestrator;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    @Scheduled(cron = "0 0 2 * * *")
    public void addNewMedia() {
        CompletableFuture<List<Long>> moviesFuture = result(this.movieIngestOrchestrator.addNewMovies(), "Movies ingest job");
        CompletableFuture<List<Long>> seriesFuture = result(this.tvSeriesIngestOrchestrator.addNewSeries(), "Series ingest job");

        CompletableFuture.allOf(moviesFuture, seriesFuture).join();

        List<Long> moviesInserted = moviesFuture.join();
        List<Long> seriesInserted = seriesFuture.join();

        boolean anyInserted = (moviesInserted.size() + seriesInserted.size()) > 0;

        if (!moviesInserted.isEmpty()) {
            logger.info(GREEN + "Publishing LatestMoviesChangedEvent ({} new movies)" + RESET, moviesInserted.size());
            this.mediaEventPublisher.publishLatestMoviesChangedEvent();
        } else {
            logger.info(YELLOW + "No new movies inserted — skipping movies event." + RESET);
        }

        if (!seriesInserted.isEmpty()) {
            logger.info(GREEN + "Publishing LatestSeriesChangedEvent ({} new series)" + RESET, seriesInserted.size());
            this.mediaEventPublisher.publishLatestSeriesChangedEvent();
        } else {
            logger.info(YELLOW + "No new series inserted — skipping series event." + RESET);
        }

        if (anyInserted) {
            logger.info(GREEN + "Publishing LatestMediaChangedEvent (movies={}, series={})" + RESET,
                    moviesInserted.size(), seriesInserted.size());
            this.mediaEventPublisher.publishLatestMediaChangedEvent();
        } else {
            logger.info(YELLOW + "No media inserted — skipping global media event." + RESET);
        }
    }

    private CompletableFuture<List<Long>> result(CompletableFuture<List<Long>> future, String jobName) {
        return future.exceptionally(ex -> {
            logger.error(RED + "{} failed" + RESET, jobName, ex);
            return new ArrayList<>();
        });
    }
}

