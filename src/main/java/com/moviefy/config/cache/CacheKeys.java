package com.moviefy.config.cache;

import java.util.Set;

public final class CacheKeys {
    private CacheKeys() {}

    public static final String MOVIE_DETAILS_BY_ID      = "movieDetailsById";
    public static final String TV_SERIES_DETAILS_BY_ID  = "tvSeriesDetailsById";

    public static final String LATEST_MOVIES            = "latestMovies";
    public static final String LATEST_TV_SERIES         = "latestTvSeries";
    public static final String LATEST_MEDIA             = "latestMedia";

    public static final String TRENDING_MOVIES          = "trendingMovies";
    public static final String TRENDING_TV_SERIES       = "trendingTvSeries";
    public static final String TRENDING_MEDIA           = "trendingMedia";

    public static final String POPULAR_MOVIES           = "popularMovies";
    public static final String POPULAR_TV_SERIES        = "popularTvSeries";
    public static final String POPULAR_MEDIA            = "popularMedia";

    public static final String TOP_RATED_MOVIES         = "topRatedMovies";
    public static final String TOP_RATED_TV_SERIES      = "topRatedTvSeries";
    public static final String TOP_RATED_MEDIA          = "topRatedMedia";

    public static final String COLLECTION_BY_NAME      = "collectionByName";
    public static final String MOVIES_HOME_BY_COLLECTION= "moviesHomeByCollection";
    public static final String MOVIES_BY_COLLECTION_API_ID = "moviesByCollectionApiId";
    public static final String HOME_SERIES_BY_COLLECTION= "homeSeriesByCollection";
    public static final String POPULAR_COLLECTIONS      = "popularCollections";

    public static final String MOVIES_BY_GENRES         = "moviesByGenres";
    public static final String TV_SERIES_BY_GENRES      = "tvSeriesByGenres";
    public static final String MEDIA_BY_GENRES          = "mediaByGenres";

    public static final String MOVIES_BY_CAST           = "moviesByCast";
    public static final String SERIES_BY_CAST           = "seriesByCast";
    public static final String MEDIA_BY_CAST            = "mediaByCast";

    public static final String MOVIES_BY_CREW           = "moviesByCrew";
    public static final String SERIES_BY_CREW           = "seriesByCrew";
    public static final String MEDIA_BY_CREW            = "mediaByCrew";

    /** A whitelist you can validate against or iterate when wiring cache manager. */
    public static final Set<String> ALL = Set.of(
            MOVIE_DETAILS_BY_ID, TV_SERIES_DETAILS_BY_ID,
            LATEST_MOVIES, LATEST_TV_SERIES, LATEST_MEDIA,
            TRENDING_MOVIES, TRENDING_TV_SERIES, TRENDING_MEDIA,
            POPULAR_MOVIES, POPULAR_TV_SERIES, POPULAR_MEDIA,
            TOP_RATED_MOVIES, TOP_RATED_TV_SERIES, TOP_RATED_MEDIA,
            COLLECTION_BY_NAME, MOVIES_HOME_BY_COLLECTION, MOVIES_BY_COLLECTION_API_ID,
            HOME_SERIES_BY_COLLECTION, POPULAR_COLLECTIONS,
            MOVIES_BY_GENRES, TV_SERIES_BY_GENRES, MEDIA_BY_GENRES,
            MOVIES_BY_CAST, SERIES_BY_CAST, MEDIA_BY_CAST,
            MOVIES_BY_CREW, SERIES_BY_CREW, MEDIA_BY_CREW
    );

    public static boolean isValid(String key) {
        return ALL.contains(key);
    }
}
