package com.moviefy.config.cache.events;

import com.moviefy.config.cache.CacheInvalidation;
import com.moviefy.config.cache.CacheKeys;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class LatestCacheListener {
    private final CacheInvalidation caches;

    public LatestCacheListener(CacheInvalidation caches) {
        this.caches = caches;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLatestChangedMovies(LatestChangedMoviesEvent e) {
        caches.clear(CacheKeys.LATEST_MOVIES);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLatestChangedTvSeries(LatestChangedTvSeriesEvent e) {
        caches.clear(CacheKeys.LATEST_TV_SERIES);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLatestChangedMedia(LatestChangedMediaEvent e) {
        caches.clear(CacheKeys.LATEST_MEDIA);
    }
}
