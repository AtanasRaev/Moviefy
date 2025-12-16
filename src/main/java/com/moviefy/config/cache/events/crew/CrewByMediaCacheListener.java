package com.moviefy.config.cache.events.crew;

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
public class CrewByMediaCacheListener {
    private final CacheManager cacheManager;

    public CrewByMediaCacheListener(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCrewByMovieChanged(CrewByMovieChangedEvent e) {
        if (e.crewIds() == null || e.crewIds().isEmpty()) {
            return;
        }
        removeByPrefix(e.crewIds(), CacheKeys.MOVIES_BY_CREW);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCrewByTvSeriesChanged(CrewByTvSeriesChangedEvent e) {
        if (e.crewIds() == null || e.crewIds().isEmpty()) {
            return;
        }
        removeByPrefix(e.crewIds(), CacheKeys.SERIES_BY_CREW);
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCrewByMediaChanged(CrewByMediaChangedEvent e) {
        if (e.crewIds() == null || e.crewIds().isEmpty()) {
            return;
        }
        removeByPrefix(e.crewIds(), CacheKeys.MEDIA_BY_CREW);
    }

    private void removeByPrefix(Set<Long> castIds, String cacheKey) {
        CaffeineCache caffeine = (CaffeineCache) cacheManager.getCache(cacheKey);
        if (caffeine == null) {
            return;
        }
        Cache<Object, Object> nativeCache = caffeine.getNativeCache();
        List<String> prefixes = castIds.stream().map(id -> "crew=" + id + ";").toList();
        nativeCache.asMap().keySet().removeIf(k ->
                (k instanceof String s) && prefixes.stream().anyMatch(s::startsWith)
        );
    }
}
