package com.moviefy.config.cache;

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
            var c = cacheManager.getCache(n);
            if (c != null) c.clear();
        }
    }
}
