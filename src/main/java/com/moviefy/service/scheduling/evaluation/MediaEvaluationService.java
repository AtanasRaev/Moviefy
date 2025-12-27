package com.moviefy.service.scheduling.evaluation;

import com.moviefy.service.scheduling.MediaEventPublisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.*;

@Service
public class MediaEvaluationService {
    private final MovieEvaluationOrchestrator movieEvaluationOrchestrator;
    private final TvSeriesEvaluationOrchestrator tvSeriesEvaluationOrchestrator;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(MediaEvaluationService.class);


    public MediaEvaluationService(MovieEvaluationOrchestrator movieEvaluationOrchestrator,
                                  TvSeriesEvaluationOrchestrator tvSeriesEvaluationOrchestrator,
                                  MediaEventPublisher mediaEventPublisher) {
        this.movieEvaluationOrchestrator = movieEvaluationOrchestrator;
        this.tvSeriesEvaluationOrchestrator = tvSeriesEvaluationOrchestrator;
        this.mediaEventPublisher = mediaEventPublisher;
    }

    @Scheduled(cron = "0 0 4 1 * *")
    public void evaluateMedia() {
        CompletableFuture<List<Long>> moviesFuture = result(this.movieEvaluationOrchestrator.evaluateMovies(), "Movies evaluation");
        CompletableFuture<List<Long>> seriesFuture = result(this.tvSeriesEvaluationOrchestrator.evaluateTvSeries(), "TvSeries evaluation");

        CompletableFuture.allOf(moviesFuture, seriesFuture).join();

        List<Long> moviesEvaluated = moviesFuture.join();
        List<Long> seriesEvaluated = seriesFuture.join();

        boolean anyInserted = (moviesEvaluated.size() + seriesEvaluated.size()) > 0;

        if (!moviesEvaluated.isEmpty()) {
            logger.info(GREEN + "Publishing LatestMoviesChangedEvent and TrendingMoviesChangedEvent ({} new movies)" + RESET, moviesEvaluated.size());
            this.mediaEventPublisher.publishTrendingMoviesChangedEvent();
            this.mediaEventPublisher.publishByGenresChangedMoviesEvent();
            this.mediaEventPublisher.publishLatestMoviesChangedEvent();
        } else {
            logger.debug(YELLOW + "No movies evaluated — skipping movies event." + RESET);
        }

        if (!seriesEvaluated.isEmpty()) {
            logger.info(GREEN + "Publishing LatestSeriesChangedEvent and TrendingSeriesChangedEvent ({} new series)" + RESET, seriesEvaluated.size());
            this.mediaEventPublisher.publishTrendingSeriesChangedEvent();
            this.mediaEventPublisher.publishByGenresChangedTvSeriesEvent();
            this.mediaEventPublisher.publishLatestSeriesChangedEvent();
        } else {
            logger.debug(YELLOW + "No new series evaluated — skipping series event." + RESET);
        }

        if (anyInserted) {
            logger.info(GREEN + "Publishing LatestMediaChangedEvent and TrendingMediaChangedEvent  (movies={}, series={}))" + RESET, moviesEvaluated.size(), seriesEvaluated.size());
            this.mediaEventPublisher.publishTrendingMediaChangedEvent();
            this.mediaEventPublisher.publishByGenresChangedMediaEvent();
            this.mediaEventPublisher.publishLatestMediaChangedEvent();
        } else {
            logger.debug(YELLOW + "No media evaluated — skipping media event." + RESET);
        }
    }

    private CompletableFuture<List<Long>> result(CompletableFuture<List<Long>> future, String jobName) {
        return future.exceptionally(ex -> {
            logger.error(RED + "{} failed" + RESET, jobName, ex);
            return new ArrayList<>();
        });
    }
}
