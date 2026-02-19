package com.moviefy.database.repository.media;

import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageProjection;
import com.moviefy.database.model.dto.pageDto.mediaDto.movieDto.MoviePageWithGenreProjection;
import com.moviefy.database.model.entity.media.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByApiId(Long apiId);

    @Query("select m.apiId from Movie m where m.collection.apiId = :collectionApiId")
    Set<Long> findApiIdsByCollectionApiId(@Param("collectionApiId") Long collectionApiId);

    @Query("SELECT COUNT(m) FROM Movie m WHERE EXTRACT(YEAR FROM m.releaseDate) = " +
            "(SELECT MAX(EXTRACT(YEAR FROM m.releaseDate)) FROM Movie m)")
    Long countNewestMovies();

    @Query("SELECT MAX(EXTRACT(YEAR FROM m.releaseDate)) FROM Movie m")
    Integer findNewestMovieYear();

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres LEFT JOIN FETCH m.productionCompanies WHERE m.apiId = :apiId")
    Optional<Movie> findMovieByApiId(@Param("apiId") Long apiId);

    @Query(
            value = """
                    SELECT DISTINCT
                        m.id AS id,
                        m.api_id AS apiId,
                        m.title AS title,
                        m.popularity AS popularity,
                        m.poster_path AS posterPath,
                        m.vote_average AS voteAverage,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        m.release_date AS releaseDate,
                        'movie' AS mediaType,
                        m.runtime AS runtime
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                      AND m.release_date <= :startDate
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT m.id)
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                      AND m.release_date <= :startDate
                    """,
            nativeQuery = true
    )
    Page<MoviePageProjection> findByReleaseDateAndGenres(
            @Param("startDate") LocalDate startDate,
            @Param("genres") List<String> genres,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT DISTINCT
                        m.id AS id,
                        m.api_id AS apiId,
                        m.title AS title,
                        m.popularity AS popularity,
                        m.poster_path AS posterPath,
                        m.vote_average AS voteAverage,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        m.release_date AS releaseDate,
                        m.vote_count AS voteCount,
                        'movie' AS mediaType,
                        m.runtime AS runtime,
                        m.trailer AS trailer,
                    
                        (
                            SELECT g2.name
                            FROM movie_genre mg2
                            JOIN movies_genres g2 ON g2.id = mg2.genre_id
                            WHERE mg2.movie_id = m.id
                            ORDER BY g2.name
                            LIMIT 1
                        ) AS genre
                    
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT m.id)
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                    """,
            nativeQuery = true
    )
    Page<MoviePageWithGenreProjection> findAllByGenresMapped(@Param("genres") List<String> genres, Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.apiId IN :apiIds")
    List<Movie> findAllByApiIdIn(Set<Long> apiIds);

    @Query(
            value = """
                    SELECT DISTINCT
                        m.id AS id,
                        m.api_id AS apiId,
                        m.title AS title,
                        m.popularity AS popularity,
                        m.poster_path AS posterPath,
                        m.vote_average AS voteAverage,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        m.release_date AS releaseDate,
                        'movie' AS mediaType,
                        m.runtime AS runtime
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT m.id)
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:genres)
                    """,
            nativeQuery = true
    )
    Page<MoviePageProjection> searchByGenres(@Param("genres") List<String> genres, Pageable pageable);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN movie_genre mg ON mg.movie_id = m.id
                        JOIN movies_genres g ON g.id = mg.genre_id
                        WHERE m.vote_count >= 50
                          AND LOWER(g.name) IN (:genres)
                    ),
                    stats AS (
                        SELECT
                            AVG(m.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY m.vote_count) AS m
                        FROM movies m
                        JOIN filtered_ids f ON f.id = m.id
                    )
                    SELECT
                        m.id                          AS id,
                        m.api_id                      AS apiId,
                        m.title                       AS title,
                        m.popularity                  AS popularity,
                        m.poster_path                 AS posterPath,
                        m.vote_average                AS voteAverage,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        m.release_date                AS releaseDate,
                        m.vote_count                  AS voteCount,
                        'movie'                       AS mediaType,
                        m.runtime                     AS runtime,
                        m.trailer                     AS trailer,
                        (
                            SELECT g2.name
                            FROM movie_genre mg2
                            JOIN movies_genres g2 ON g2.id = mg2.genre_id
                            WHERE mg2.movie_id = m.id
                            ORDER BY g2.name
                            LIMIT 1
                        )                             AS genre,
                        (
                            (m.vote_count / (m.vote_count + stats.m)) * m.vote_average
                          + (stats.m      / (m.vote_count + stats.m)) * stats.C
                        )                             AS score
                    FROM movies m
                    JOIN filtered_ids f ON f.id = m.id
                    CROSS JOIN stats
                    ORDER BY score DESC, m.vote_count DESC, m.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN movie_genre mg ON mg.movie_id = m.id
                        JOIN movies_genres g ON g.id = mg.genre_id
                        WHERE m.vote_count >= 50
                          AND LOWER(g.name) IN (:genres)
                    ) x
                    """,
            nativeQuery = true
    )
    Page<MoviePageWithGenreProjection> findTopRatedByGenres(@Param("genres") List<String> genres, Pageable pageable);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN cast_movies mc ON mc.movie_id = m.id
                        WHERE mc.cast_id = :castId
                    ),
                    stats AS (
                        SELECT
                            AVG(m.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY m.vote_count) AS m
                        FROM movies m
                        JOIN filtered_ids f ON f.id = m.id
                    )
                    SELECT
                        m.id                           AS id,
                        m.api_id                       AS apiId,
                        m.title                        AS title,
                        m.popularity                   AS popularity,
                        m.poster_path                  AS posterPath,
                        m.vote_average                 AS voteAverage,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        m.release_date                 AS releaseDate,
                        m.vote_count                   AS voteCount,
                        'movie'                        AS mediaType,
                        m.runtime                      AS runtime,
                        m.trailer                      AS trailer,
                        (
                          (m.vote_count / (m.vote_count + stats.m)) * m.vote_average
                        + (stats.m      / (m.vote_count + stats.m)) * stats.C
                        )                              AS score
                    FROM movies m
                    JOIN filtered_ids f ON f.id = m.id
                    CROSS JOIN stats
                    ORDER BY score DESC NULLS LAST, m.vote_count DESC, m.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN cast_movies mc ON mc.movie_id = m.id
                        WHERE mc.cast_id = :castId
                    ) x
                    """,
            nativeQuery = true
    )
    Page<MoviePageProjection> findTopRatedMoviesByCastId(@Param("castId") long castId,
                                                         Pageable pageable);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN crew_movies mc ON mc.movie_id = m.id
                        WHERE mc.crew_id = :crewId
                    ),
                    stats AS (
                        SELECT
                            AVG(m.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY m.vote_count) AS m
                        FROM movies m
                        JOIN filtered_ids f ON f.id = m.id
                    )
                    SELECT
                        m.id                           AS id,
                        m.api_id                       AS apiId,
                        m.title                        AS title,
                        m.popularity                   AS popularity,
                        m.poster_path                  AS posterPath,
                        m.vote_average                 AS voteAverage,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        m.release_date                 AS releaseDate,
                        m.vote_count                   AS voteCount,
                        'movie'                        AS mediaType,
                        m.runtime                      AS runtime,
                        m.trailer                      AS trailer,
                        (
                          (m.vote_count / (m.vote_count + stats.m)) * m.vote_average
                        + (stats.m      / (m.vote_count + stats.m)) * stats.C
                        )                              AS score
                    FROM movies m
                    JOIN filtered_ids f ON f.id = m.id
                    CROSS JOIN stats
                    ORDER BY score DESC NULLS LAST, m.vote_count DESC, m.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN crew_movies mc ON mc.movie_id = m.id
                        WHERE mc.crew_id = :crewId
                    ) x
                    """,
            nativeQuery = true
    )
    Page<MoviePageProjection> findTopRatedMoviesByCrewId(@Param("crewId") long crewId,
                                                         Pageable pageable);

    @Query(
            value = """
                    WITH filtered_ids AS (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN movie_production mp ON mp.movie_id = m.id
                        WHERE mp.production_id = :productionId
                    ),
                    stats AS (
                        SELECT
                            AVG(m.vote_average) AS C,
                            PERCENTILE_CONT(0.80) WITHIN GROUP (ORDER BY m.vote_count) AS m
                        FROM movies m
                        JOIN filtered_ids f ON f.id = m.id
                    )
                    SELECT
                        m.id                           AS id,
                        m.api_id                       AS apiId,
                        m.title                        AS title,
                        m.popularity                   AS popularity,
                        m.poster_path                  AS posterPath,
                        m.vote_average                 AS voteAverage,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        m.release_date                 AS releaseDate,
                        m.vote_count                   AS voteCount,
                        'movie'                        AS mediaType,
                        m.runtime                      AS runtime,
                        m.trailer                      AS trailer,
                        (
                          (m.vote_count / (m.vote_count + stats.m)) * m.vote_average
                        + (stats.m      / (m.vote_count + stats.m)) * stats.C
                        )                              AS score
                    FROM movies m
                    JOIN filtered_ids f ON f.id = m.id
                    CROSS JOIN stats
                    ORDER BY score DESC NULLS LAST, m.vote_count DESC, m.id
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN movie_production mp ON mp.movie_id = m.id
                        WHERE mp.production_id = :productionId
                    ) x
                    """,
            nativeQuery = true
    )
    Page<MoviePageProjection> findTopRatedMoviesByProductionCompanyId(@Param("productionId") long productionId, Pageable pageable);

    @Query(value = """
              SELECT m.api_id
              FROM movies m
              WHERE m.api_id IN (:ids)
            """, nativeQuery = true)
    Set<Long> findIdsAllByApiIdIn(@Param("ids") Collection<Long> ids);

    @Query(value = """
              SELECT COUNT(*)
              FROM movies
              WHERE ranking_year = :year
                AND favourite_count = 0
            """, nativeQuery = true)
    long findCountByRankingYear(@Param("year") int year);

    @Query(value = """
              SELECT *
              FROM movies
              WHERE ranking_year = :year
                AND favourite_count = 0
                AND inserted_at < NOW() - INTERVAL '14 days'
              ORDER BY
                vote_count,
                popularity,
                id DESC
              LIMIT 1
            """, nativeQuery = true)
    Optional<Movie> findLowestRatedMovieByRankingYear(@Param("year") int year);

    @Query(value = """
                      SELECT *
                      FROM movies
                      ORDER BY
                        popularity DESC
                    LIMIT :limit
            """, nativeQuery = true)
    List<Movie> findAllByPopularityDesc(@Param("limit") int limit);

    @Query(value = """
                SELECT *
                FROM movies m
                WHERE m.inserted_at BETWEEN :startDate AND :endDate
                  AND (m.ranking_year = :currentYear OR m.ranking_year = :previousYear)
                  AND (
                        m.refreshed_at IS NULL
                        OR m.refreshed_at < (
                            CAST(:now AS timestamp)
                            - (CAST(:cooldownDays AS int) * INTERVAL '1 day')
                        )
                      )
                ORDER BY COALESCE(m.refreshed_at, TIMESTAMP '1970-01-01'),
                         m.inserted_at,
                         m.id
                LIMIT :limit
            """, nativeQuery = true)
    List<Movie> findMoviesDueForRefresh(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("currentYear") int currentYear,
            @Param("previousYear") int previousYear,
            @Param("now") LocalDateTime now,
            @Param("cooldownDays") int cooldownDays,
            @Param("limit") int limit
    );

    @Modifying
    @Query(value = "DELETE FROM user_favorite_movies WHERE movie_id = :movieId", nativeQuery = true)
    int deleteFavoritesByMovieId(@Param("movieId") long movieId);

    @Query("SELECT m.apiId FROM Movie m WHERE m.apiId IN :apiIds")
    Set<Long> findAllApiIdsByApiIdIn(@Param("apiIds") Set<Long> apiIds);

    @Query(value = """
                SELECT DISTINCT
                    m.id AS id,
                    m.api_id AS apiId,
                    m.title AS title,
                    m.popularity AS popularity,
                    m.poster_path AS posterPath,
                    m.vote_average AS voteAverage,
                    CAST(date_part('year', m.release_date) AS integer) AS year,
                    m.release_date AS releaseDate,
                    m.vote_count AS voteCount,
                    'movie' AS mediaType,
                    m.runtime AS runtime,
                    m.trailer AS trailer,
            
                    (
                        SELECT g2.name
                        FROM movie_genre mg2
                        JOIN movies_genres g2 ON g2.id = mg2.genre_id
                        WHERE mg2.movie_id = m.id
                        ORDER BY g2.name
                        LIMIT 1
                    ) AS genre
            
                FROM user_favorite_movies ufm
                JOIN movies m ON m.id = ufm.movie_id
                WHERE ufm.user_id = :userId
                ORDER BY m.release_date DESC NULLS LAST, m.popularity DESC
            """, nativeQuery = true)
    List<MoviePageWithGenreProjection> findFavoriteMovies(@Param("userId") Long userId);

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
//                      SELECT m.*,
//                             ts_rank_cd(m.search_vector, (SELECT tsq FROM ts)) AS fts_rank,
//                             GREATEST(
//                               similarity(m.title_norm,          (SELECT q_sep FROM q)),
//                               similarity(m.original_title_norm, (SELECT q_sep FROM q))
//                             ) AS tri_sim,
//                             GREATEST(
//                               similarity(regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q)),
//                               similarity(regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q))
//                             ) AS tri_sim_compact,
//                             CASE WHEN m.title_norm = (SELECT q_sep FROM q) THEN 1.0 ELSE 0 END AS exact_boost,
//                             CASE WHEN m.title_norm LIKE (SELECT q_sep FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost,
//                             CASE WHEN regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') = (SELECT q_compact FROM q) THEN 1.0 ELSE 0 END AS exact_boost_compact,
//                             CASE WHEN regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') LIKE (SELECT q_compact FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost_compact,
//                             regexp_replace(m.title_norm,          '[[:punct:]]', ' ', 'g') AS title_sep,
//                             regexp_replace(m.original_title_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
//                             regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') AS title_compact,
//                             regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
//                      FROM movies m
//                      WHERE
//                        (m.search_vector @@ (SELECT tsq FROM ts))
//                        OR (m.title_norm % (SELECT q_sep FROM q))
//                        OR (m.original_title_norm % (SELECT q_sep FROM q))
//                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                    ),
//                    must AS (
//                      SELECT
//                        c.*,
//                        CASE WHEN c.title_sep ~* (SELECT q_phrase_rx FROM q)
//                               OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
//                             THEN 6.0 ELSE 0 END AS phrase_boost,
//                        s.hits,
//                        cardinality(t.tokens) AS need_hits,
//                        CASE WHEN s.hits = cardinality(t.tokens) THEN 2.0 ELSE 0 END AS all_token_boost,
//                        (cardinality(t.tokens) - s.hits) AS missing_tokens,
//                        (
//                          c.title_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.title_sep ~* (SELECT q_phrase_rx FROM q)
//                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
//                        ) AS title_match
//                      FROM cand c
//                      CROSS JOIN tok t
//                      CROSS JOIN LATERAL (
//                        SELECT COALESCE(SUM(
//                                 CAST(
//                                   (
//                                     (c.title_sep     ~* ('\\m' || w || '\\M'))
//                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
//                                     OR (c.title_compact ILIKE ('%' || w || '%'))
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
//                      title_match
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
//                      title
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
//                        regexp_replace(m.title_norm,          '[[:punct:]]', ' ', 'g') AS title_sep,
//                        regexp_replace(m.original_title_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
//                        regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') AS title_compact,
//                        regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
//                      FROM movies m
//                      WHERE
//                        (m.search_vector @@ (SELECT tsq FROM ts))
//                        OR (m.title_norm % (SELECT q_sep FROM q))
//                        OR (m.original_title_norm % (SELECT q_sep FROM q))
//                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
//                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
//                    ),
//                    must AS (
//                      SELECT
//                        s.hits,
//                        cardinality(t.tokens) AS need_hits,
//                        (
//                          c.title_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
//                          OR c.title_sep ~* (SELECT q_phrase_rx FROM q)
//                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
//                        ) AS title_match
//                      FROM cand c
//                      CROSS JOIN tok t
//                      CROSS JOIN LATERAL (
//                        SELECT COALESCE(SUM(
//                                 CAST(
//                                   (
//                                     (c.title_sep     ~* ('\\m' || w || '\\M'))
//                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
//                                     OR (c.title_compact ILIKE ('%' || w || '%'))
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
//                      title_match
//                      AND hits >= CASE WHEN need_hits >= 3 THEN need_hits - 1 ELSE need_hits END
//                    """,
//            nativeQuery = true
//    )
//    Page<Movie> searchByTitle(@Param("query") String query, Pageable pageable);
}
