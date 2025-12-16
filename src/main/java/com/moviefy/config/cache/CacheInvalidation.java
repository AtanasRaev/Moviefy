package com.moviefy.config.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Component;

@Component
public class CacheInvalidation {
    private final CacheManager cacheManager;

    public CacheInvalidation(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public void clear(String... names) {
        for (String n : names) {
            Cache cache = cacheManager.getCache(n);
            if (cache != null) {
                cache.clear();
            }
        }
    }

    public void evict(String name, Object key) {
        Cache cache = cacheManager.getCache(name);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
