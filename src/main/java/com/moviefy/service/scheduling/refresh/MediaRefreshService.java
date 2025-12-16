package com.moviefy.service.scheduling.refresh;

import com.moviefy.service.scheduling.MediaEventPublisher;
import com.moviefy.service.scheduling.refresh.movie.MovieRefreshService;
import com.moviefy.service.scheduling.refresh.tvSeries.TvSeriesRefreshService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static com.moviefy.utils.Ansi.*;

@Service
public class MediaRefreshService {
    private final MovieRefreshService movieRefreshService;
    private final TvSeriesRefreshService tvSeriesRefreshService;
    private final MediaEventPublisher mediaEventPublisher;

    private static final Logger logger = LoggerFactory.getLogger(MediaRefreshService.class);

    public MediaRefreshService(MovieRefreshService movieRefreshService,
                               TvSeriesRefreshService tvSeriesRefreshService,
                               MediaEventPublisher mediaEventPublisher) {
        this.movieRefreshService = movieRefreshService;
        this.tvSeriesRefreshService = tvSeriesRefreshService;
        this.mediaEventPublisher = mediaEventPublisher;
    }

//    @Scheduled(cron = "0 34 19 * * *", zone = "Europe/Sofia")
    public void refreshMedia() {
        CompletableFuture<List<Long>> moviesFuture = result(this.movieRefreshService.refreshMovies(), "Movies refresh");
        CompletableFuture<List<Long>> seriesFuture = result(this.tvSeriesRefreshService.refreshTvSeries(),  "TvSeries refresh");

        CompletableFuture.allOf(moviesFuture, seriesFuture).join();

        List<Long> moviesRefreshed = moviesFuture.join();
        List<Long> seriesRefreshed = seriesFuture.join();

        boolean anyRefreshed = (moviesRefreshed.size() + seriesRefreshed.size()) > 0;

        if (!moviesRefreshed.isEmpty()) {
            List<Long> distinctIds = moviesRefreshed.stream().distinct().toList();
            logger.info(GREEN + "Publishing TrendingMoviesChangedEvent ({} updated movies)" + RESET, distinctIds.size());
            this.mediaEventPublisher.publishTrendingMoviesChangedEvent();
            this.mediaEventPublisher.publishByGenresChangedMoviesEvent();
            this.mediaEventPublisher.publishMoviesDetailsChangedEvent(distinctIds);
        } else {
            logger.debug(YELLOW + "No movies updated — skipping movies event." + RESET);
        }

        if (!seriesRefreshed.isEmpty()) {
            List<Long> distinctIds = seriesRefreshed.stream().distinct().toList();
            logger.info(GREEN + "Publishing TrendingSeriesChangedEvent ({} updated series)" + RESET, seriesRefreshed);
            this.mediaEventPublisher.publishTrendingSeriesChangedEvent();
            this.mediaEventPublisher.publishByGenresChangedTvSeriesEvent();
            this.mediaEventPublisher.publishTvSeriesDetailsChangedEvent(distinctIds);
        } else {
            logger.debug(YELLOW + "No series updated — skipping series event." + RESET);
        }

        if (anyRefreshed) {
            logger.info(GREEN + "Publishing TrendingMediaChangedEvent (movies={}, series={}))" + RESET, moviesRefreshed.size(), seriesRefreshed.size());
            this.mediaEventPublisher.publishTrendingMediaChangedEvent();
            this.mediaEventPublisher.publishByGenresChangedMediaEvent();
        } else {
            logger.debug(YELLOW + "No media updated — skipping media event." + RESET);
        }
    }

    private CompletableFuture<List<Long>> result(CompletableFuture<List<Long>> future, String jobName) {
        return future.exceptionally(ex -> {
            logger.error(RED + "{} failed" + RESET, jobName, ex);
            return new ArrayList<>();
        });
    }
}
