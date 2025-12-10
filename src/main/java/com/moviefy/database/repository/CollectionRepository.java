package com.moviefy.database.repository;

import com.moviefy.database.model.dto.pageDto.CollectionPageProjection;
import com.moviefy.database.model.entity.media.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Optional<Collection> findByApiId(Long apiId);

    @Query("SELECT c FROM Collection c WHERE c.name ILIKE :name")
    List<Collection> findByName(@Param("name") String name);

    @Query("SELECT c FROM Collection c WHERE c.name IN :names")
    List<Collection> findAllByNameIn(List<String> names);

    @Query("SELECT c FROM Collection c JOIN c.movies m WHERE m.id = :movieId")
    Optional<Collection> findCollectionsByMovieId(@Param("movieId") Long movieId);

    @Query(
            value = """
                      WITH oldest AS (
                        SELECT DISTINCT ON (m.collection_id)
                               m.collection_id,
                               m.id
                        FROM movies m
                        ORDER BY m.collection_id,
                                 m.release_date NULLS LAST,
                                 m.id
                      )
                      SELECT
                        c.name         AS name,
                        c.poster_path  AS posterPath,
                        c.api_id       AS apiId,
                        m.overview     AS overview,
                        m.runtime      AS runtime,
                        m.vote_average AS voteAverage
                      FROM collections c
                      LEFT JOIN oldest o ON o.collection_id = c.id
                      LEFT JOIN movies  m ON m.id = o.id
                      WHERE C.has_movies = TRUE
                        AND c.poster_path IS NOT NULL
                      ORDER BY c.vote_count_average DESC NULLS LAST
                    """,
            countQuery = """
                      SELECT COUNT(*) FROM collections c
                    """,
            nativeQuery = true
    )
    Page<CollectionPageProjection> findAllByVoteCountAverageDesc(Pageable pageable);

    @Query(
            value = """
                WITH matched AS (
                    SELECT c.id, c.name, c.poster_path, c.api_id
                    FROM collections c
                    WHERE
                          c.poster_path IS NOT NULL
                          AND (
                              :q IS NULL OR :q = '' OR
                              (
                                SELECT COUNT(*)
                                FROM regexp_split_to_table(LOWER(:q), '\\s+') AS w
                                WHERE length(w) >= 2
                                  AND w NOT IN ('and','the','of','a','an','&')
                                  AND LOWER(
                                        regexp_replace(
                                          regexp_replace(c.name, '[[:punct:]]', '', 'g'),
                                          '\\s+', '', 'g'
                                        )
                                      ) LIKE '%' ||
                                      LOWER(
                                        regexp_replace(
                                          regexp_replace(w, '[[:punct:]]', '', 'g'),
                                          '\\s+', '', 'g'
                                        )
                                      ) || '%'
                              ) = (
                                SELECT COUNT(*)
                                FROM regexp_split_to_table(LOWER(:q), '\\s+') AS w
                                WHERE length(w) >= 2
                                  AND w NOT IN ('and','the','of','a','an','&')
                              )
                          )
                ),
                oldest AS (
                    SELECT DISTINCT ON (m.collection_id)
                           m.collection_id,
                           m.id
                    FROM movies m
                    ORDER BY
                        m.collection_id,
                        m.release_date ASC NULLS LAST,
                        m.id ASC
                )
                SELECT
                    matched.name        AS name,
                    matched.poster_path AS posterPath,
                    matched.api_id      AS apiId,
                    m.overview          AS overview,
                    m.runtime           AS runtime,
                    m.vote_average      AS voteAverage
                FROM matched
                LEFT JOIN oldest o ON o.collection_id = matched.id
                LEFT JOIN movies  m ON m.id = o.id
                ORDER BY matched.name, matched.id
                """,
            countQuery = """
                WITH matched AS (
                    SELECT c.id
                    FROM collections c
                    WHERE
                          c.poster_path IS NOT NULL
                          AND (
                              :q IS NULL OR :q = '' OR
                              (
                                SELECT COUNT(*)
                                FROM regexp_split_to_table(LOWER(:q), '\\s+') AS w
                                WHERE length(w) >= 2
                                  AND w NOT IN ('and','the','of','a','an','&')
                                  AND LOWER(
                                        regexp_replace(
                                          regexp_replace(c.name, '[[:punct:]]', '', 'g'),
                                          '\\s+', '', 'g'
                                        )
                                      ) LIKE '%' ||
                                      LOWER(
                                        regexp_replace(
                                          regexp_replace(w, '[[:punct:]]', '', 'g'),
                                          '\\s+', '', 'g'
                                        )
                                      ) || '%'
                              ) = (
                                SELECT COUNT(*)
                                FROM regexp_split_to_table(LOWER(:q), '\\s+') AS w
                                WHERE length(w) >= 2
                                  AND w NOT IN ('and','the','of','a','an','&')
                              )
                          )
                )
                SELECT COUNT(*) FROM matched
                """,
            nativeQuery = true
    )
    Page<CollectionPageProjection> searchCollectionByName(@Param("q") String query, Pageable pageable);


}
