package com.moviefy.config.cache.events.details;

import com.moviefy.config.cache.CacheInvalidation;
import com.moviefy.config.cache.CacheKeys;
import com.moviefy.database.repository.media.MovieRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.Set;

@Component
public class MediaDetailsCacheListener {
    private final MovieRepository movieRepository;
    private final CacheInvalidation caches;

    public MediaDetailsCacheListener(MovieRepository movieRepository,
                                     CacheInvalidation caches) {
        this.movieRepository = movieRepository;
        this.caches = caches;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMoviesRefreshed(MoviesDetailsChangedEvent e) {
        if (e.ids() == null || e.ids().isEmpty()) {
            return;
        }
        e.ids().forEach(id -> caches.evict(CacheKeys.MOVIE_DETAILS_BY_ID, id));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onTvSeriesRefreshed(TvSeriesDetailsChangedEvent e) {
        if (e.ids() == null || e.ids().isEmpty()) {
            return;
        }
        e.ids().forEach(id -> caches.evict(CacheKeys.TV_SERIES_DETAILS_BY_ID, id));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMoviesDetailsByCollectionChanged(MoviesDetailsByCollectionChangedEvent e) {
        if (e.collectionApiId() == null) return;
        Set<Long> ids = movieRepository.findApiIdsByCollectionApiId(e.collectionApiId());
        ids.forEach(id -> caches.evict(CacheKeys.MOVIE_DETAILS_BY_ID, id));
    }
}
