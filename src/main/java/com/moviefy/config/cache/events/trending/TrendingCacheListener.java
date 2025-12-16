package com.moviefy.config.cache.events.trending;

import com.moviefy.config.cache.CacheInvalidation;
import com.moviefy.config.cache.CacheKeys;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class TrendingCacheListener {
    private final CacheInvalidation caches;

    public TrendingCacheListener(CacheInvalidation caches) {
        this.caches = caches;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTrendingChangedMovies(TrendingChangedMoviesEvent e) {
        caches.clear(CacheKeys.TRENDING_MOVIES);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTrendingChangedTvSeries(TrendingChangedTvSeriesEvent e) {
        caches.clear(CacheKeys.TRENDING_TV_SERIES);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTrendingChangedMedia(TrendingChangedMediaEvent e) {
        caches.clear(CacheKeys.TRENDING_MEDIA);
    }
}
