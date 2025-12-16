package com.moviefy.config.cache.events.cast;

import com.github.benmanes.caffeine.cache.Cache;
import com.moviefy.config.cache.CacheKeys;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;
import java.util.Set;

@Component
public class CastByMediaCacheListener {
    private final CacheManager cacheManager;

    public CastByMediaCacheListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCastByMovieChanged(CastByMovieChangedEvent e) {
        if (e.castIds() == null || e.castIds().isEmpty()) {
            return;
        }
        removeByPrefix(e.castIds(), CacheKeys.MOVIES_BY_CAST);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCastByTvSeriesChanged(CastByTvSeriesChangedEvent e) {
        if (e.castIds() == null || e.castIds().isEmpty()) {
            return;
        }
        removeByPrefix(e.castIds(), CacheKeys.SERIES_BY_CAST);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCastByMediaChanged(CastByMediaChangedEvent e) {
        if (e.castIds() == null || e.castIds().isEmpty()) {
            return;
        }
        removeByPrefix(e.castIds(), CacheKeys.MEDIA_BY_CAST);
    }

    private void removeByPrefix(Set<Long> castIds, String cacheKey) {
        CaffeineCache caffeine = (CaffeineCache) cacheManager.getCache(cacheKey);
        if (caffeine == null) {
            return;
        }
        Cache<Object, Object> nativeCache = caffeine.getNativeCache();
        List<String> prefixes = castIds.stream().map(id -> "cast=" + id + ";").toList();
        nativeCache.asMap().keySet().removeIf(k ->
                (k instanceof String s) && prefixes.stream().anyMatch(s::startsWith)
        );
    }
}
