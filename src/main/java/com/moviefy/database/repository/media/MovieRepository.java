package com.moviefy.database.repository.media;

import com.moviefy.database.model.entity.media.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByApiId(Long apiId);

    @Query("SELECT COUNT(m) FROM Movie m WHERE EXTRACT(YEAR FROM m.releaseDate) = " +
            "(SELECT MAX(EXTRACT(YEAR FROM m.releaseDate)) FROM Movie m)")
    Long countNewestMovies();

    @Query("SELECT MAX(EXTRACT(YEAR FROM m.releaseDate)) FROM Movie m")
    Integer findNewestMovieYear();

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres LEFT JOIN FETCH m.productionCompanies WHERE m.apiId = :apiId")
    Optional<Movie> findMovieByApiId(@Param("apiId") Long apiId);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :startDate ORDER BY m.releaseDate DESC, m.id")
    Page<Movie> findByReleaseDate(@Param("startDate") LocalDate startDate, Pageable pageable);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.genres g WHERE g.name = :genreName")
    List<Movie> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT m FROM Movie m ORDER BY m.popularity DESC")
    Page<Movie> findAllByPopularityDesc(Pageable pageable);

    @Query("SELECT m FROM Movie m")
    Page<Movie> findAllSortedByVoteCount(Pageable pageable);

    @Query("SELECT m FROM Movie m WHERE m.apiId IN :apiIds")
    List<Movie> findAllByApiIdIn(Set<Long> apiIds);

    @Query(
            value = """
                    WITH q AS (
                      SELECT
                        regexp_replace(
                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
                          '\\s+', ' ', 'g'
                        ) AS q_sep,
                        (
                          '\\m' ||
                          regexp_replace(
                            regexp_replace(
                              immutable_unaccent(lower(:query)),
                              '([\\.\\^\\$\\*\\+\\?\\(\\)\\[\\]\\{\\}\\|\\\\])', '\\\\\\\\1', 'g'
                            ),
                            '\\\\-', '[- ]', 'g'
                          ) ||
                          '\\M'
                        ) AS q_phrase_rx,
                        regexp_replace(
                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
                          '\\s+', '', 'g'
                        ) AS q_compact
                    ),
                    tok AS (
                      SELECT ARRAY(
                        SELECT DISTINCT w
                        FROM regexp_split_to_table((SELECT q_sep FROM q), '\\s+') AS w
                        WHERE length(w) >= 2
                          AND w NOT IN ('a','an','the','to','of','and','or','in','on')
                      ) AS tokens
                    ),
                    ts AS ( SELECT plainto_tsquery('simple', (SELECT q_sep FROM q)) AS tsq ),
                    cand AS (
                      SELECT m.*,
                             ts_rank_cd(m.search_vector, (SELECT tsq FROM ts)) AS fts_rank,
                             GREATEST(
                               similarity(m.title_norm,          (SELECT q_sep FROM q)),
                               similarity(m.original_title_norm, (SELECT q_sep FROM q))
                             ) AS tri_sim,
                             GREATEST(
                               similarity(regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q)),
                               similarity(regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q))
                             ) AS tri_sim_compact,
                             CASE WHEN m.title_norm = (SELECT q_sep FROM q) THEN 1.0 ELSE 0 END AS exact_boost,
                             CASE WHEN m.title_norm LIKE (SELECT q_sep FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost,
                             CASE WHEN regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') = (SELECT q_compact FROM q) THEN 1.0 ELSE 0 END AS exact_boost_compact,
                             CASE WHEN regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') LIKE (SELECT q_compact FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost_compact,
                             regexp_replace(m.title_norm,          '[[:punct:]]', ' ', 'g') AS title_sep,
                             regexp_replace(m.original_title_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
                             regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') AS title_compact,
                             regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
                      FROM movies m
                      WHERE
                        (m.search_vector @@ (SELECT tsq FROM ts))
                        OR (m.title_norm % (SELECT q_sep FROM q))
                        OR (m.original_title_norm % (SELECT q_sep FROM q))
                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                    ),
                    must AS (
                      SELECT
                        c.*,
                        CASE WHEN c.title_sep ~* (SELECT q_phrase_rx FROM q)
                               OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
                             THEN 6.0 ELSE 0 END AS phrase_boost,
                        s.hits,
                        cardinality(t.tokens) AS need_hits,
                        CASE WHEN s.hits = cardinality(t.tokens) THEN 2.0 ELSE 0 END AS all_token_boost,
                        (cardinality(t.tokens) - s.hits) AS missing_tokens,
                        (
                          c.title_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.title_sep ~* (SELECT q_phrase_rx FROM q)
                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
                        ) AS title_match
                      FROM cand c
                      CROSS JOIN tok t
                      CROSS JOIN LATERAL (
                        SELECT COALESCE(SUM(
                                 CAST(
                                   (
                                     (c.title_sep     ~* ('\\m' || w || '\\M'))
                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
                                     OR (c.title_compact ILIKE ('%' || w || '%'))
                                     OR (c.orig_compact  ILIKE ('%' || w || '%'))
                                   ) AS int
                                 )
                               ), 0) AS hits
                        FROM unnest(t.tokens) AS w
                      ) s
                    )
                    SELECT *
                    FROM must
                    WHERE
                      title_match
                      AND hits >= CASE WHEN need_hits >= 3 THEN need_hits - 1 ELSE need_hits END
                    ORDER BY
                      (exact_boost_compact * 5.0) DESC,
                      (prefix_boost_compact * 3.0) DESC,
                      (exact_boost * 2.0) DESC,
                      (prefix_boost * 1.5) DESC,
                      (phrase_boost * 1.2) DESC,
                      (tri_sim_compact * 1.0) DESC,
                      (tri_sim * 0.6) DESC,
                      (fts_rank * 0.8) DESC,
                      (all_token_boost * 1.0) DESC,
                      (missing_tokens * -1.5) DESC,
                      title
                    """,
            countQuery = """
                    WITH q AS (
                      SELECT
                        regexp_replace(
                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
                          '\\s+', ' ', 'g'
                        ) AS q_sep,
                        (
                          '\\m' ||
                          regexp_replace(
                            regexp_replace(
                              immutable_unaccent(lower(:query)),
                              '([\\.\\^\\$\\*\\+\\?\\(\\)\\[\\]\\{\\}\\|\\\\])', '\\\\\\\\1', 'g'
                            ),
                            '\\\\-', '[- ]', 'g'
                          ) ||
                          '\\M'
                        ) AS q_phrase_rx,
                        regexp_replace(
                          regexp_replace(immutable_unaccent(lower(:query)), '[[:punct:]]', ' ', 'g'),
                          '\\s+', '', 'g'
                        ) AS q_compact
                    ),
                    tok AS (
                      SELECT ARRAY(
                        SELECT DISTINCT w
                        FROM regexp_split_to_table((SELECT q_sep FROM q), '\\s+') AS w
                        WHERE length(w) >= 2
                          AND w NOT IN ('a','an','the','to','of','and','or','in','on')
                      ) AS tokens
                    ),
                    ts AS ( SELECT plainto_tsquery('simple', (SELECT q_sep FROM q)) AS tsq ),
                    cand AS (
                      SELECT
                        regexp_replace(m.title_norm,          '[[:punct:]]', ' ', 'g') AS title_sep,
                        regexp_replace(m.original_title_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
                        regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') AS title_compact,
                        regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
                      FROM movies m
                      WHERE
                        (m.search_vector @@ (SELECT tsq FROM ts))
                        OR (m.title_norm % (SELECT q_sep FROM q))
                        OR (m.original_title_norm % (SELECT q_sep FROM q))
                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(m.title_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                        OR (regexp_replace(m.original_title_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                    ),
                    must AS (
                      SELECT
                        s.hits,
                        cardinality(t.tokens) AS need_hits,
                        (
                          c.title_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.title_sep ~* (SELECT q_phrase_rx FROM q)
                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
                        ) AS title_match
                      FROM cand c
                      CROSS JOIN tok t
                      CROSS JOIN LATERAL (
                        SELECT COALESCE(SUM(
                                 CAST(
                                   (
                                     (c.title_sep     ~* ('\\m' || w || '\\M'))
                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
                                     OR (c.title_compact ILIKE ('%' || w || '%'))
                                     OR (c.orig_compact  ILIKE ('%' || w || '%'))
                                   ) AS int
                                 )
                               ), 0) AS hits
                        FROM unnest(t.tokens) AS w
                      ) s
                    )
                    SELECT COUNT(*)
                    FROM must
                    WHERE
                      title_match
                      AND hits >= CASE WHEN need_hits >= 3 THEN need_hits - 1 ELSE need_hits END
                    """,
            nativeQuery = true
    )
    Page<Movie> searchByTitle(@Param("query") String query, Pageable pageable);

    @Query(
            value = """
                    SELECT DISTINCT m
                    FROM Movie m
                    JOIN m.genres g
                    WHERE LOWER(g.name) IN :genres
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT m.id)
                    FROM Movie m
                    JOIN m.genres g
                    WHERE LOWER(g.name) IN :genres
                    """
    )
    Page<Movie> searchByGenres(@Param("genres") List<String> genres, Pageable pageable);

}
