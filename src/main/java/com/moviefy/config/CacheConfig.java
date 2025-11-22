package com.moviefy.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Expiry;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.cache.annotation.EnableCaching;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        CaffeineCache movieDetailsById = new CaffeineCache(
                "movieDetailsById",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache tvSeriesDetailsById = new CaffeineCache(
                "tvSeriesDetailsById",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache latestMovies = new CaffeineCache(
                "latestMovies",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache latestTvSeries = new CaffeineCache(
                "latestTvSeries",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache latestMedia = new CaffeineCache(
                "latestMedia",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache trendingMovies = new CaffeineCache(
                "trendingMovies",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache trendingTvSeries = new CaffeineCache(
                "trendingTvSeries",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache trendingMedia = new CaffeineCache(
                "trendingMedia",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache popularMovies = new CaffeineCache(
                "popularMovies",
                baseBuilderWithJan1Expiry()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache popularTvSeries = new CaffeineCache(
                "popularTvSeries",
                baseBuilderWithJan1Expiry()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache popularMedia = new CaffeineCache(
                "popularMedia",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );


        CaffeineCache topRatedMovies = new CaffeineCache(
                "topRatedMovies",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache topRatedTvSeries = new CaffeineCache(
                "topRatedTvSeries",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache topRatedMedia = new CaffeineCache(
                "topRatedMedia",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache collectionsByName = new CaffeineCache(
                "collectionsByName",
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .build()
        );

        CaffeineCache moviesHomeByCollection = new CaffeineCache(
                "moviesHomeByCollection",
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .build()
        );

        CaffeineCache moviesByApiId = new CaffeineCache(
                "moviesByApiId",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache homeSeriesByCollection = new CaffeineCache(
                "homeSeriesByCollection",
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .build()
        );

        CaffeineCache popularCollections = new CaffeineCache(
                "popularCollections",
                Caffeine.newBuilder()
                        .maximumSize(1)
                        .build()
        );

        CaffeineCache moviesByGenres = new CaffeineCache(
                "moviesByGenres",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache tvSeriesByGenres = new CaffeineCache(
                "tvSeriesByGenres",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        CaffeineCache mediaByGenres = new CaffeineCache(
                "mediaByGenres",
                Caffeine.newBuilder()
                        .maximumSize(100)
                        .build()
        );

        SimpleCacheManager manager = new SimpleCacheManager();
        manager.setCaches(List.of(
                movieDetailsById,
                tvSeriesDetailsById,
                latestMovies,
                latestTvSeries,
                latestMedia,
                trendingMovies,
                trendingTvSeries,
                trendingMedia,
                popularMovies,
                popularTvSeries,
                popularMedia,
                topRatedMovies,
                topRatedTvSeries,
                topRatedMedia,
                collectionsByName,
                moviesHomeByCollection,
                moviesByApiId,
                homeSeriesByCollection,
                popularCollections,
                moviesByGenres,
                tvSeriesByGenres,
                mediaByGenres
        ));
        return manager;
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
