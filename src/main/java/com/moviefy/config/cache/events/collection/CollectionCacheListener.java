package com.moviefy.config.cache.events.collection;

import com.moviefy.config.cache.CacheInvalidation;
import com.moviefy.config.cache.CacheKeys;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class CollectionCacheListener {
    private final CacheInvalidation caches;
    public CollectionCacheListener(CacheInvalidation caches) { this.caches = caches; }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onCollectionChanged(CollectionChangedEvent e) {
        String key = e.name();
        caches.evict(CacheKeys.COLLECTION_BY_NAME, key);
        caches.evict(CacheKeys.MOVIES_HOME_BY_COLLECTION, key);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    public void onMoviesByCollectionChanged(MoviesByCollectionChangedEvent e) {
        Long key = e.apiId();
        caches.evict(CacheKeys.MOVIES_BY_COLLECTION_API_ID, key);
    }
}
