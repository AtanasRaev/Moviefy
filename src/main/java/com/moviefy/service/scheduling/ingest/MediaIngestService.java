package com.moviefy.service.scheduling.ingest;

import com.moviefy.service.scheduling.MediaEventPublisher;
import com.moviefy.service.scheduling.ingest.movie.MovieIngestJob;
import com.moviefy.service.scheduling.ingest.tvSeries.TvSeriesIngestJob;
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
    private final MovieIngestJob movieIngestJob;
    private final TvSeriesIngestJob tvSeriesIngestJob;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(MediaIngestService.class);


    public MediaIngestService(MovieIngestJob movieIngestJob,
                              TvSeriesIngestJob tvSeriesIngestJob,
                              MediaEventPublisher mediaEventPublisher) {
        this.movieIngestJob = movieIngestJob;
        this.tvSeriesIngestJob = tvSeriesIngestJob;
        this.mediaEventPublisher = mediaEventPublisher;
    }

//    @Scheduled(cron = "0 13 19 * * *", zone = "Europe/Sofia")
    public void addNewMedia() {
        CompletableFuture<List<Long>> moviesFuture = result(this.movieIngestJob.addNewMovies(), "Movies ingest job");
        CompletableFuture<List<Long>> seriesFuture = result(this.tvSeriesIngestJob.addNewSeries(), "Series ingest job");

        CompletableFuture.allOf(moviesFuture, seriesFuture).join();

        List<Long> moviesInserted = moviesFuture.join();
        List<Long> seriesInserted = seriesFuture.join();

        boolean anyInserted = (moviesInserted.size() + seriesInserted.size()) > 0;

        if (!moviesInserted.isEmpty()) {
            logger.info(GREEN + "Publishing LatestMoviesChangedEvent ({} new movies)" + RESET, moviesInserted);
            this.mediaEventPublisher.publishLatestMoviesChangedEvent();
        } else {
            logger.debug(YELLOW + "No new movies inserted — skipping movies event." + RESET);
        }

        if (!seriesInserted.isEmpty()) {
            logger.info(GREEN + "Publishing LatestSeriesChangedEvent ({} new series)" + RESET, seriesInserted);
            this.mediaEventPublisher.publishLatestSeriesChangedEvent();
        } else {
            logger.debug(YELLOW + "No new series inserted — skipping series event." + RESET);
        }

        if (anyInserted) {
            logger.info(GREEN + "Publishing LatestMediaChangedEvent (movies={}, series={})" + RESET,
                    moviesInserted, seriesInserted);
            this.mediaEventPublisher.publishLatestMediaChangedEvent();
        } else {
            logger.debug(YELLOW + "No media inserted — skipping global media event." + RESET);
        }
    }

    private CompletableFuture<List<Long>> result(CompletableFuture<List<Long>> future, String jobName) {
        return future.exceptionally(ex -> {
            logger.error(RED + "{} failed" + RESET, jobName, ex);
            return new ArrayList<>();
        });
    }
}

