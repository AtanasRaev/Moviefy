package com.moviefy.database.repository.media.tvSeries;

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
            "LEFT JOIN FETCH tv.statusTvSeries " +
            "WHERE tv.apiId = :apiId")
    Optional<TvSeries> findTvSeriesByApiId(@Param("apiId") Long apiId);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.firstAirDate <= :startDate ORDER BY tv.firstAirDate DESC, tv.id")
    Page<TvSeries> findByFirstAirDate(@Param("startDate") LocalDate startDate, Pageable pageable);

    @Query("SELECT tv FROM TvSeries tv WHERE EXTRACT(YEAR FROM tv.firstAirDate) BETWEEN :startYear AND :endYear ORDER BY tv.voteCount DESC")
    Page<TvSeries> findAllByYearRangeOrderByVoteCount(@Param("startYear") int startYear, @Param("endYear") int endYear, Pageable pageable);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.id IN :ids ORDER BY tv.voteCount DESC")
    Page<TvSeries> findAllBySeasonsIds(@Param("ids") List<Long> ids, Pageable pageable);

    @Query("SELECT tv FROM TvSeries tv LEFT JOIN FETCH tv.genres g WHERE g.name = :genreName")
    List<TvSeries> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT tv FROM TvSeries tv ORDER BY tv.voteCount DESC")
    Page<TvSeries> findAllSortedByVoteCount(Pageable pageable);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.name IN :names")
    List<TvSeries> findAllByNames(@Param("names") List<String> names);

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
                      SELECT s.*,
                             ts_rank_cd(s.search_vector, (SELECT tsq FROM ts)) AS fts_rank,
                             GREATEST(
                               similarity(s.name_norm,          (SELECT q_sep FROM q)),
                               similarity(s.original_name_norm, (SELECT q_sep FROM q))
                             ) AS tri_sim,
                             GREATEST(
                               similarity(regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q)),
                               similarity(regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g'), (SELECT q_compact FROM q))
                             ) AS tri_sim_compact,
                             CASE WHEN s.name_norm = (SELECT q_sep FROM q) THEN 1.0 ELSE 0 END AS exact_boost,
                             CASE WHEN s.name_norm LIKE (SELECT q_sep FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost,
                             CASE WHEN regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') = (SELECT q_compact FROM q) THEN 1.0 ELSE 0 END AS exact_boost_compact,
                             CASE WHEN regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') LIKE (SELECT q_compact FROM q) || '%' THEN 0.6 ELSE 0 END AS prefix_boost_compact,
                             regexp_replace(s.name_norm,          '[[:punct:]]', ' ', 'g') AS name_sep,
                             regexp_replace(s.original_name_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
                             regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') AS name_compact,
                             regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
                      FROM tv_series s
                      WHERE
                        (s.search_vector @@ (SELECT tsq FROM ts))
                        OR (s.name_norm % (SELECT q_sep FROM q))
                        OR (s.original_name_norm % (SELECT q_sep FROM q))
                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                    ),
                    must AS (
                      SELECT
                        c.*,
                        CASE WHEN c.name_sep ~* (SELECT q_phrase_rx FROM q)
                               OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
                             THEN 6.0 ELSE 0 END AS phrase_boost,
                        s.hits,
                        cardinality(t.tokens) AS need_hits,
                        CASE WHEN s.hits = cardinality(t.tokens) THEN 2.0 ELSE 0 END AS all_token_boost,
                        (cardinality(t.tokens) - s.hits) AS missing_tokens,
                        (
                          c.name_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.name_sep ~* (SELECT q_phrase_rx FROM q)
                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
                        ) AS name_match
                      FROM cand c
                      CROSS JOIN tok t
                      CROSS JOIN LATERAL (
                        SELECT COALESCE(SUM(
                                 CAST(
                                   (
                                     (c.name_sep     ~* ('\\m' || w || '\\M'))
                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
                                     OR (c.name_compact ILIKE ('%' || w || '%'))
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
                      name_match
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
                      name
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
                        regexp_replace(s.name_norm,          '[[:punct:]]', ' ', 'g') AS name_sep,
                        regexp_replace(s.original_name_norm, '[[:punct:]]', ' ', 'g') AS orig_sep,
                        regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') AS name_compact,
                        regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') AS orig_compact
                      FROM tv_series s
                      WHERE
                        (s.search_vector @@ (SELECT tsq FROM ts))
                        OR (s.name_norm % (SELECT q_sep FROM q))
                        OR (s.original_name_norm % (SELECT q_sep FROM q))
                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') % (SELECT q_compact FROM q))
                        OR (regexp_replace(s.name_norm,          '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                        OR (regexp_replace(s.original_name_norm, '[[:punct:][:space:]]', '', 'g') ILIKE '%' || (SELECT q_compact FROM q) || '%')
                    ),
                    must AS (
                      SELECT
                        s.hits,
                        cardinality(t.tokens) AS need_hits,
                        (
                          c.name_compact ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.orig_compact  ILIKE ('%' || (SELECT q_compact FROM q) || '%')
                          OR c.name_sep ~* (SELECT q_phrase_rx FROM q)
                          OR c.orig_sep  ~* (SELECT q_phrase_rx FROM q)
                        ) AS name_match
                      FROM cand c
                      CROSS JOIN tok t
                      CROSS JOIN LATERAL (
                        SELECT COALESCE(SUM(
                                 CAST(
                                   (
                                     (c.name_sep     ~* ('\\m' || w || '\\M'))
                                     OR (c.orig_sep   ~* ('\\m' || w || '\\M'))
                                     OR (c.name_compact ILIKE ('%' || w || '%'))
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
                      name_match
                      AND hits >= CASE WHEN need_hits >= 3 THEN need_hits - 1 ELSE need_hits END
                    """,
            nativeQuery = true
    )
    Page<TvSeries> searchByName(@Param("query") String query, Pageable pageable);


    @Query(
            value = """
                    SELECT DISTINCT tv
                    FROM TvSeries tv
                    JOIN tv.genres g
                    WHERE g.name IN :genres
                    """,
            countQuery = """
                    SELECT COUNT(DISTINCT tv.id)
                    FROM TvSeries tv
                    JOIN tv.genres g
                    WHERE g.name IN :genres
                    """
    )
    Page<TvSeries> searchByGenres(@Param("genres") List<String> genres, Pageable pageable);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.apiId IN :apiIds")
    List<TvSeries> findAllByApiIdIn(@Param("apiIds") Set<Long> apiIds);
}
