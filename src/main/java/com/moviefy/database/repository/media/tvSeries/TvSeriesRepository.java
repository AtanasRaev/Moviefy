package com.moviefy.database.repository.media.tvSeries;

import com.moviefy.database.model.dto.pageDto.mediaDto.tvSeriesDto.TvSeriesPageProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.tvSeriesDto.TvSeriesPageWithGenreProjection;
import com.moviefy.database.model.entity.media.tvSeries.TvSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface TvSeriesRepository extends JpaRepository<TvSeries, Long> {
    @Query("SELECT COUNT(tv) FROM TvSeries tv WHERE EXTRACT(YEAR FROM tv.firstAirDate) = " +
            "(SELECT MAX(EXTRACT(YEAR FROM tv.firstAirDate)) FROM TvSeries tv)")
    Long countNewestTvSeries();

    @Query("SELECT MAX(EXTRACT(YEAR FROM tv.firstAirDate)) FROM TvSeries tv")
    int findNewestTvSeriesYear();

    Optional<TvSeries> findByApiId(Long id);

    @Query("SELECT DISTINCT tv FROM TvSeries tv " +
            "LEFT JOIN FETCH tv.genres " +
            "LEFT JOIN FETCH tv.productionCompanies " +
            "LEFT JOIN FETCH tv.seasons " +
            "WHERE tv.apiId = :apiId")
    Optional<TvSeries> findTvSeriesByApiId(@Param("apiId") Long apiId);

    @Query(
            value = """
                    SELECT DISTINCT
                        tv.id AS id,
                        tv.api_id AS apiId,
                        tv.name AS name,
                        tv.popularity AS popularity,
                        tv.poster_path AS posterPath,
                        tv.vote_average AS voteAverage,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        tv.first_air_date AS releaseDate,
                        'series' AS mediaType,
                        s.seasons_count AS seasonsCount,
                        s.episodes_count AS episodesCount
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    LEFT JOIN (
                        SELECT
                            stv.tv_series_id,
                            COUNT(*) AS seasons_count,
                            COALESCE(SUM(stv.episode_count), 0) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    ) s ON s.tv_series_id = tv.id
                    WHERE LOWER(g.name) IN (:genres)
                      AND LOWER(tv.type) IN (:types)
                      AND tv.first_air_date <= :startDate
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT tv.id)
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                      AND LOWER(tv.type) IN (:types)
                      AND tv.first_air_date <= :startDate
                    """,
            nativeQuery = true
    )
    Page<TvSeriesPageProjection> findByFirstAirDateAndGenres(
            @Param("startDate") LocalDate startDate,
            @Param("genres") List<String> genres,
            @Param("types") List<String> types,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT DISTINCT
                        tv.id AS id,
                        tv.api_id AS apiId,
                        tv.name AS name,
                        tv.popularity AS popularity,
                        tv.poster_path AS posterPath,
                        tv.vote_average AS voteAverage,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        'series' AS mediaType,
                        tv.trailer AS trailer,
                        tv.vote_count AS voteCount,
                        s.seasons_count AS seasonsCount,
                        s.episodes_count AS episodesCount,
                    
                        (
                            SELECT g2.name
                            FROM series_genre mg2
                            JOIN series_genres g2 ON g2.id = mg2.genre_id
                            WHERE mg2.series_id = tv.id
                            ORDER BY g2.name
                            LIMIT 1
                        ) AS genre
                    
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    LEFT JOIN (
                        SELECT
                            stv.tv_series_id,
                            COUNT(*) AS seasons_count,
                            COALESCE(SUM(stv.episode_count), 0) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    ) s ON s.tv_series_id = tv.id
                    WHERE LOWER(g.name) IN (:genres)
                      AND LOWER(tv.type) IN (:types)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT tv.id)
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                      AND LOWER(tv.type) IN (:types)
                    """,
            nativeQuery = true
    )
    Page<TvSeriesPageWithGenreProjection> findAllByGenresMapped(@Param("genres") List<String> genres, @Param("types") List<String> types, Pageable pageable);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.name IN :names")
    List<TvSeries> findAllByNames(@Param("names") List<String> names);

    @Query(
            value = """
                    SELECT DISTINCT
                        tv.id AS id,
                        tv.api_id AS apiId,
                        tv.name AS name,
                        tv.popularity AS popularity,
                        tv.poster_path AS posterPath,
                        tv.vote_average AS voteAverage,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        tv.first_air_date AS releaseDate,
                        'series' AS mediaType,
                        s.seasons_count AS seasonsCount,
                        s.episodes_count AS episodesCount
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    LEFT JOIN (
                        SELECT
                            stv.tv_series_id,
                            COUNT(*) AS seasons_count,
                            COALESCE(SUM(stv.episode_count), 0) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    ) s ON s.tv_series_id = tv.id
                    WHERE LOWER(g.name) IN (:genres)
                      AND LOWER(tv.type) IN (:types)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT tv.id)
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                      AND LOWER(tv.type) IN (:types)
                    """,
            nativeQuery = true
    )
    Page<TvSeriesPageProjection> searchByGenres(@Param("genres") List<String> genres, @Param("types") List<String> types, Pageable pageable);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.apiId IN :apiIds")
    List<TvSeries> findAllByApiIdIn(@Param("apiIds") Set<Long> apiIds);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN series_genre tsg ON tsg.series_id = tv.id
                        JOIN series_genres g   ON g.id = tsg.genre_id
                        WHERE tv.vote_count >= 50
                          AND LOWER(g.name) IN (:genres)
                          AND LOWER(tv.type) IN (:types)
                    ),
                    stats AS (
                        SELECT
                            AVG(tv.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY tv.vote_count) AS m
                        FROM tv_series tv
                        JOIN filtered_ids f ON f.id = tv.id
                    ),
                    seasons_agg AS (
                        SELECT
                            stv.tv_series_id,
                            COUNT(*)                         AS seasons_count,
                            COALESCE(SUM(stv.episode_count), 0) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    )
                    SELECT
                        tv.id                                            AS id,
                        tv.api_id                                        AS apiId,
                        tv.name                                          AS name,
                        tv.popularity                                    AS popularity,
                        tv.poster_path                                   AS posterPath,
                        tv.vote_average                                  AS voteAverage,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        'series'                                         AS mediaType,
                        tv.trailer                                       AS trailer,
                        tv.vote_count                                    AS voteCount,
                        sa.seasons_count                                 AS seasonsCount,
                        sa.episodes_count                                AS episodesCount,
                        (
                            SELECT g2.name
                            FROM series_genre mg2
                            JOIN series_genres g2 ON g2.id = mg2.genre_id
                            WHERE mg2.series_id = tv.id
                            ORDER BY g2.name
                            LIMIT 1
                        )                                                AS genre,
                        (
                            (tv.vote_count / (tv.vote_count + COALESCE(stats.m, 500))) * tv.vote_average
                          + (COALESCE(stats.m, 500) / (tv.vote_count + COALESCE(stats.m, 500))) * stats.C
                        )                                                AS score
                    FROM tv_series tv
                    JOIN filtered_ids f ON f.id = tv.id
                    CROSS JOIN stats
                    LEFT JOIN seasons_agg sa ON sa.tv_series_id = tv.id
                    ORDER BY score DESC, tv.vote_count DESC, tv.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN series_genre tsg ON tsg.series_id = tv.id
                        JOIN series_genres g   ON g.id = tsg.genre_id
                        WHERE tv.vote_count >= 50
                          AND LOWER(g.name) IN (:genres)
                          AND LOWER(tv.type) IN (:types)
                    ) x
                    """,
            nativeQuery = true
    )
    Page<TvSeriesPageWithGenreProjection> findTopRatedSeriesByGenresAndTypes(@Param("genres") List<String> genres, @Param("types")  List<String> types, Pageable pageable);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN cast_tv tc ON tc.tv_series_id = tv.id
                        WHERE tc.cast_id = :castId
                    ),
                    stats AS (
                        SELECT
                            AVG(tv.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY tv.vote_count) AS m
                        FROM tv_series tv
                        JOIN filtered_ids f ON f.id = tv.id
                    ),
                    seasons_agg AS (
                        SELECT
                            stv.tv_series_id,
                            COUNT(*)                           AS seasons_count,
                            COALESCE(SUM(stv.episode_count), 0) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    )
                    SELECT
                        tv.id                                            AS id,
                        tv.api_id                                        AS apiId,
                        tv.name                                          AS name,
                        tv.popularity                                    AS popularity,
                        tv.poster_path                                   AS posterPath,
                        tv.vote_average                                  AS voteAverage,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        'series'                                         AS mediaType,
                        tv.trailer                                       AS trailer,
                        tv.vote_count                                    AS voteCount,
                        sa.seasons_count                                 AS seasonsCount,
                        sa.episodes_count                                AS episodesCount,
                        (
                          (tv.vote_count / (tv.vote_count + COALESCE(stats.m, 500))) * tv.vote_average
                        + (COALESCE(stats.m, 500) / (tv.vote_count + COALESCE(stats.m, 500))) * stats.C
                        )                                                AS score
                    FROM tv_series tv
                    JOIN filtered_ids f ON f.id = tv.id
                    CROSS JOIN stats
                    LEFT JOIN seasons_agg sa ON sa.tv_series_id = tv.id
                    ORDER BY score DESC, tv.vote_count DESC, tv.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN cast_tv tc ON tc.tv_series_id = tv.id
                        WHERE tc.cast_id = :castId
                    ) x
                    """,
            nativeQuery = true
    )
    Page<TvSeriesPageProjection> findTopRatedSeriesByCastId(@Param("castId") long castId, Pageable pageable);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN crew_tv tc ON tc.tv_series_id = tv.id
                        WHERE tc.crew_id = :crewId
                    ),
                    stats AS (
                        SELECT
                            AVG(tv.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY tv.vote_count) AS m
                        FROM tv_series tv
                        JOIN filtered_ids f ON f.id = tv.id
                    ),
                    seasons_agg AS (
                        SELECT
                            stv.tv_series_id,
                            COUNT(*)                           AS seasons_count,
                            COALESCE(SUM(stv.episode_count), 0) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    )
                    SELECT
                        tv.id                                            AS id,
                        tv.api_id                                        AS apiId,
                        tv.name                                          AS name,
                        tv.popularity                                    AS popularity,
                        tv.poster_path                                   AS posterPath,
                        tv.vote_average                                  AS voteAverage,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        'series'                                         AS mediaType,
                        tv.trailer                                       AS trailer,
                        tv.vote_count                                    AS voteCount,
                        sa.seasons_count                                 AS seasonsCount,
                        sa.episodes_count                                AS episodesCount,
                        (
                          (tv.vote_count / (tv.vote_count + COALESCE(stats.m, 500))) * tv.vote_average
                        + (COALESCE(stats.m, 500) / (tv.vote_count + COALESCE(stats.m, 500))) * stats.C
                        )                                                AS score
                    FROM tv_series tv
                    JOIN filtered_ids f ON f.id = tv.id
                    CROSS JOIN stats
                    LEFT JOIN seasons_agg sa ON sa.tv_series_id = tv.id
                    ORDER BY score DESC, tv.vote_count DESC, tv.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN crew_tv tc ON tc.tv_series_id = tv.id
                        WHERE tc.crew_id = :crewId
                    ) x
                    """,
            nativeQuery = true
    )
    Page<TvSeriesPageProjection> findTopRatedSeriesByCrewId(@Param("crewId") long crewId, Pageable pageable);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN tv_series_production tsp ON tsp.series_id = tv.id
                        WHERE tsp.production_id = :productionId
                    ),
                    stats AS (
                        SELECT
                            AVG(tv.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY tv.vote_count) AS m
                        FROM tv_series tv
                        JOIN filtered_ids f ON f.id = tv.id
                    ),
                    seasons_agg AS (
                        SELECT
                            stv.tv_series_id,
                            COUNT(*)                           AS seasons_count,
                            COALESCE(SUM(stv.episode_count), 0) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    )
                    SELECT
                        tv.id                                            AS id,
                        tv.api_id                                        AS apiId,
                        tv.name                                          AS name,
                        tv.popularity                                    AS popularity,
                        tv.poster_path                                   AS posterPath,
                        tv.vote_average                                  AS voteAverage,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        'series'                                         AS mediaType,
                        tv.trailer                                       AS trailer,
                        tv.vote_count                                    AS voteCount,
                        sa.seasons_count                                 AS seasonsCount,
                        sa.episodes_count                                AS episodesCount,
                        (
                          (tv.vote_count / (tv.vote_count + COALESCE(stats.m, 500))) * tv.vote_average
                        + (COALESCE(stats.m, 500) / (tv.vote_count + COALESCE(stats.m, 500))) * stats.C
                        )                                                AS score
                    FROM tv_series tv
                    JOIN filtered_ids f ON f.id = tv.id
                    CROSS JOIN stats
                    LEFT JOIN seasons_agg sa ON sa.tv_series_id = tv.id
                    ORDER BY score DESC, tv.vote_count DESC, tv.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN tv_series_production tsp ON tsp.series_id = tv.id
                        WHERE tsp.production_id = :productionId
                    ) x
                    """,
            nativeQuery = true
    )
    Page<TvSeriesPageProjection> findTopRatedSeriesByProductionCompanyId(@Param("productionId") long id, Pageable pageable);

    @Query(value = """
              SELECT COUNT(*)
              FROM tv_series
              WHERE ranking_year = :year
                AND favourite_count = 0
            """, nativeQuery = true)
    long findCountByRankingYear(@Param("year") int rankingYear);

    @Query(value = """
              SELECT *
              FROM tv_series
              WHERE ranking_year = :year
                AND favourite_count = 0
                AND inserted_at < NOW() - INTERVAL '14 days'
              ORDER BY
                vote_count,
                popularity,
                id DESC
              LIMIT 1
            """, nativeQuery = true)
    Optional<TvSeries> findLowestRatedSeriesByRankingYear(@Param("year") int rankingYear);

    @Query(value = """
              SELECT tv.api_id
              FROM tv_series tv
              WHERE tv.api_id IN (:ids)
            """, nativeQuery = true)
    Set<Long> findIdsAllByApiIdIn(@Param("ids") Set<Long> incomingIds);

//    @Query(
//            value = """
//                    WITH q AS (
//                      SELECT
//                        regexp_replace(
//                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
//                          '\\s+', ' ', 'g'
//                        ) AS q_sep,
//                        (
//                          '\\m' ||
//                          regexp_replace(
//                            regexp_replace(
//                              immutable_unaccent(lower(:query)),
//                              '([\\.\\^\\$\\*\\+\\?\\(\\)\\[\\]\\{\\}\\|\\\\])', '\\\\\\\\1', 'g'
//                            ),
//                            '\\\\-', '[- ]', 'g'
//                          ) ||
//                          '\\M'
//                        ) AS q_phrase_rx,
//                        regexp_replace(
//                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
//                          '\\s+', '', 'g'
//                        ) AS q_compact
//                    ),
//                    tok AS (
//                      SELECT ARRAY(
//                        SELECT DISTINCT w
//                        FROM regexp_split_to_table((SELECT q_sep FROM q), '\\s+') AS w
//                        WHERE length(w) >= 2
//                          AND w NOT IN ('a','an','the','to','of','and','or','in','on')
//                      ) AS tokens
//                    ),
//                    ts AS ( SELECT plainto_tsquery('simple', (SELECT q_sep FROM q)) AS tsq ),
//                    cand AS (
//                      SELECT s.*,
//                             ts_rank_cd(s.search_vector, (SELECT tsq FROM ts)) AS fts_rank,
//                             GREATEST(
//                               similarity(s.name_norm,          (SELECT q_sep FROM q)),
//                               similarity(s.original_name_norm, (SELECT q_sep FROM q))
//                             ) AS tri_sim,
//                             GREATEST(
//                               similarity(regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q)),
//                               similarity(regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q))
//                             ) AS tri_sim_compact,
//                             CASE WHEN s.name_norm = (SELECT q_sep FROM q) THEN 1.0 ELSE 0 END AS exact_boost,
//                             CASE WHEN s.name_norm LIKE (SELECT q_sep FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost,
//                             CASE WHEN regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') = (SELECT q_compact FROM q) THEN 1.0 ELSE 0 END AS exact_boost_compact,
//                             CASE WHEN regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') LIKE (SELECT q_compact FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost_compact,
//                             regexp_replace(s.name_norm,          '[[:punct:]]', ' ', 'g') AS name_sep,
//                             regexp_replace(s.original_name_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
//                             regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') AS name_compact,
//                             regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
//                      FROM tv_series s
//                      WHERE
//                        (s.search_vector @@ (SELECT tsq FROM ts))
//                        OR (s.name_norm % (SELECT q_sep FROM q))
//                        OR (s.original_name_norm % (SELECT q_sep FROM q))
//                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                    ),
//                    must AS (
//                      SELECT
//                        c.*,
//                        CASE WHEN c.name_sep ~* (SELECT q_phrase_rx FROM q)
//                               OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
//                             THEN 6.0 ELSE 0 END AS phrase_boost,
//                        s.hits,
//                        cardinality(t.tokens) AS need_hits,
//                        CASE WHEN s.hits = cardinality(t.tokens) THEN 2.0 ELSE 0 END AS all_token_boost,
//                        (cardinality(t.tokens) - s.hits) AS missing_tokens,
//                        (
//                          c.name_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.name_sep ~* (SELECT q_phrase_rx FROM q)
//                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
//                        ) AS name_match
//                      FROM cand c
//                      CROSS JOIN tok t
//                      CROSS JOIN LATERAL (
//                        SELECT COALESCE(SUM(
//                                 CAST(
//                                   (
//                                     (c.name_sep     ~* ('\\m' || w || '\\M'))
//                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
//                                     OR (c.name_compact ILIKE ('%' || w || '%'))
//                                     OR (c.orig_compact  ILIKE ('%' || w || '%'))
//                                   ) AS int
//                                 )
//                               ), 0) AS hits
//                        FROM unnest(t.tokens) AS w
//                      ) s
//                    )
//                    SELECT *
//                    FROM must
//                    WHERE
//                      name_match
//                      AND hits >= CASE WHEN need_hits >= 3 THEN need_hits - 1 ELSE need_hits END
//                    ORDER BY
//                      (exact_boost_compact * 5.0) DESC,
//                      (prefix_boost_compact * 3.0) DESC,
//                      (exact_boost * 2.0) DESC,
//                      (prefix_boost * 1.5) DESC,
//                      (phrase_boost * 1.2) DESC,
//                      (tri_sim_compact * 1.0) DESC,
//                      (tri_sim * 0.6) DESC,
//                      (fts_rank * 0.8) DESC,
//                      (all_token_boost * 1.0) DESC,
//                      (missing_tokens * -1.5) DESC,
//                      name
//                    """,
//            countQuery = """
//                    WITH q AS (
//                      SELECT
//                        regexp_replace(
//                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
//                          '\\s+', ' ', 'g'
//                        ) AS q_sep,
//                        (
//                          '\\m' ||
//                          regexp_replace(
//                            regexp_replace(
//                              immutable_unaccent(lower(:query)),
//                              '([\\.\\^\\$\\*\\+\\?\\(\\)\\[\\]\\{\\}\\|\\\\])', '\\\\\\\\1', 'g'
//                            ),
//                            '\\\\-', '[- ]', 'g'
//                          ) ||
//                          '\\M'
//                        ) AS q_phrase_rx,
//                        regexp_replace(
//                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
//                          '\\s+', '', 'g'
//                        ) AS q_compact
//                    ),
//                    tok AS (
//                      SELECT ARRAY(
//                        SELECT DISTINCT w
//                        FROM regexp_split_to_table((SELECT q_sep FROM q), '\\s+') AS w
//                        WHERE length(w) >= 2
//                          AND w NOT IN ('a','an','the','to','of','and','or','in','on')
//                      ) AS tokens
//                    ),
//                    ts AS ( SELECT plainto_tsquery('simple', (SELECT q_sep FROM q)) AS tsq ),
//                    cand AS (
//                      SELECT
//                        regexp_replace(s.name_norm,          '[[:punct:]]', ' ', 'g') AS name_sep,
//                        regexp_replace(s.original_name_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
//                        regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') AS name_compact,
//                        regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
//                      FROM tv_series s
//                      WHERE
//                        (s.search_vector @@ (SELECT tsq FROM ts))
//                        OR (s.name_norm % (SELECT q_sep FROM q))
//                        OR (s.original_name_norm % (SELECT q_sep FROM q))
//                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                    ),
//                    must AS (
//                      SELECT
//                        s.hits,
//                        cardinality(t.tokens) AS need_hits,
//                        (
//                          c.name_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.name_sep ~* (SELECT q_phrase_rx FROM q)
//                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
//                        ) AS name_match
//                      FROM cand c
//                      CROSS JOIN tok t
//                      CROSS JOIN LATERAL (
//                        SELECT COALESCE(SUM(
//                                 CAST(
//                                   (
//                                     (c.name_sep     ~* ('\\m' || w || '\\M'))
//                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
//                                     OR (c.name_compact ILIKE ('%' || w || '%'))
//                                     OR (c.orig_compact  ILIKE ('%' || w || '%'))
//                                   ) AS int
//                                 )
//                               ), 0) AS hits
//                        FROM unnest(t.tokens) AS w
//                      ) s
//                    )
//                    SELECT COUNT(*)
//                    FROM must
//                    WHERE
//                      name_match
//                      AND hits >= CASE WHEN need_hits >= 3 THEN need_hits - 1 ELSE need_hits END
//                    """,
//            nativeQuery = true
//    )
//    Page<TvSeries> searchByName(@Param("query") String query, Pageable pageable);
}
