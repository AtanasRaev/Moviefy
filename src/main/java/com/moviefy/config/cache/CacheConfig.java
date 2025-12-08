package com.moviefy.config.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        Caffeine<Object, Object> default100 = defaultBuilder().maximumSize(100);
        Caffeine<Object, Object> jan1Expiry100 = baseBuilderWithJan1Expiry().maximumSize(100);
        Caffeine<Object, Object> singleEntry = Caffeine.newBuilder().maximumSize(1);

        List<CaffeineCache> caches = new ArrayList<>();

        addAll(caches, default100,
                CacheKeys.MOVIE_DETAILS_BY_ID,
                CacheKeys.TV_SERIES_DETAILS_BY_ID,

                CacheKeys.LATEST_MOVIES,
                CacheKeys.LATEST_TV_SERIES,
                CacheKeys.LATEST_MEDIA,

                CacheKeys.TRENDING_MOVIES,
                CacheKeys.TRENDING_TV_SERIES,
                CacheKeys.TRENDING_MEDIA,

                CacheKeys.POPULAR_MEDIA,
                CacheKeys.TOP_RATED_MOVIES,
                CacheKeys.TOP_RATED_TV_SERIES,
                CacheKeys.TOP_RATED_MEDIA,

                CacheKeys.MOVIES_BY_API_ID,
                CacheKeys.POPULAR_COLLECTIONS,

                CacheKeys.MOVIES_BY_GENRES,
                CacheKeys.TV_SERIES_BY_GENRES,
                CacheKeys.MEDIA_BY_GENRES,

                CacheKeys.MOVIES_BY_CAST,
                CacheKeys.SERIES_BY_CAST,
                CacheKeys.MEDIA_BY_CAST,

                CacheKeys.MOVIES_BY_CREW,
                CacheKeys.SERIES_BY_CREW,
                CacheKeys.MEDIA_BY_CREW
        );

        addAll(caches, jan1Expiry100,
                CacheKeys.POPULAR_MOVIES,
                CacheKeys.POPULAR_TV_SERIES
        );

        addAll(caches, singleEntry,
                CacheKeys.COLLECTIONS_BY_NAME,
                CacheKeys.MOVIES_HOME_BY_COLLECTION,
                CacheKeys.HOME_SERIES_BY_COLLECTION
        );

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(caches);
        return manager;
    }

    /** Small helper to add multiple caches with the same builder. */
    private void addAll(List<CaffeineCache> out, Caffeine<Object, Object> builder, String... names) {
        for (String name : names) {
            out.add(newCache(name, builder));
        }
    }

    /** Factory for a single CaffeineCache with the given builder. */
    private CaffeineCache newCache(String name, Caffeine<Object, Object> builder) {
        return new CaffeineCache(name, builder.build());
    }

    /** Common default builder (no special expiry). */
    private Caffeine<Object, Object> defaultBuilder() {
        return Caffeine.newBuilder();
    }

    /**
     * Common builder that applies "expire on Jan 1 at 00:00" semantics.
     * You can reuse this to create more caches with the same calendar-based expiry.
     */
    private Caffeine<Object, Object> baseBuilderWithJan1Expiry() {
        return Caffeine.newBuilder()
                .expireAfter(new Expiry<Object, Object>() {
                    @Override
                    public long expireAfterCreate(Object key, Object value, long currentTime) {
                        return nanosUntilNextJanuaryFirst();
                    }

                    @Override
                    public long expireAfterUpdate(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }

                    @Override
                    public long expireAfterRead(Object key, Object value, long currentTime, long currentDuration) {
                        return currentDuration;
                    }
                });
    }

    /**
     * Computes nanoseconds until next January 1st at 00:00 in the system zone.
     * If you want a fixed zone (e.g., Sofia), use ZoneId.of("Europe/Sofia").
     */
    private long nanosUntilNextJanuaryFirst() {
        ZoneId zone = ZoneId.systemDefault();
        ZonedDateTime now = ZonedDateTime.now(zone);

        ZonedDateTime janFirstThisYear = ZonedDateTime.of(
                LocalDate.of(now.getYear(), 1, 1),
                LocalTime.MIDNIGHT,
                zone
        );

        ZonedDateTime nextReset = now.isBefore(janFirstThisYear)
                ? janFirstThisYear
                : ZonedDateTime.of(LocalDate.of(now.getYear() + 1, 1, 1), LocalTime.MIDNIGHT, zone);

        long millis = Math.max(1, nextReset.toInstant().toEpochMilli() - now.toInstant().toEpochMilli());
        return TimeUnit.MILLISECONDS.toNanos(millis);
    }
}
