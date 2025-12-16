package com.moviefy.config.cache.events.byGenres;

import com.moviefy.config.cache.CacheInvalidation;
import com.moviefy.config.cache.CacheKeys;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class ByGenresCacheListener {
    private final CacheInvalidation caches;

    public ByGenresCacheListener(CacheInvalidation caches) {
        this.caches = caches;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onByGenresChangedMovies(ByGenresChangedMoviesEvent e) {
        caches.clear(CacheKeys.MOVIES_BY_GENRES);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onByGenresChangedTvSeries(ByGenresChangedTvSeriesEvent e) {
        caches.clear(CacheKeys.TV_SERIES_BY_GENRES);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onByGenresChangedMedia(ByGenresChangedMediaEvent e) {
        caches.clear(CacheKeys.MEDIA_BY_GENRES);
    }
}
