package com.moviefy.database.repository.media;

import com.moviefy.database.model.dto.pageDto.MediaProjection;
import com.moviefy.database.model.dto.pageDto.MediaWithGenreProjection;
import com.moviefy.database.model.entity.media.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MediaRepository extends JpaRepository<Movie, Long> {
    @Query(
            value = """
                    SELECT DISTINCT
                        u.id AS id,
                        u.api_id AS apiId,
                        u.title AS title,
                        u.popularity AS popularity,
                        u.poster_path AS posterPath,
                        u.vote_average AS voteAverage,
                        u.year AS year,
                        u.media_type AS mediaType,
                        u.seasons_count AS seasonsCount,
                        u.episodes_count AS episodesCount,
                        u.runtime AS runtime,
                        u.release_date AS releaseDate
                    FROM (
                        SELECT
                            m.id,
                            m.api_id,
                            m.title,
                            m.popularity,
                            m.poster_path,
                            m.vote_average,
                            CAST(date_part('year', m.release_date) AS integer) AS year,
                            'movie' AS media_type,
                            CAST(NULL AS integer) AS seasons_count,
                            CAST(NULL AS integer) AS episodes_count,
                            m.runtime,
                            m.release_date AS release_date
                        FROM movies m
                        JOIN movie_genre mg ON mg.movie_id = m.id
                        JOIN movies_genres g ON g.id = mg.genre_id
                        WHERE LOWER(g.name) IN (:movieGenres)
                    
                        UNION ALL
                    
                        SELECT
                            tv.id,
                            tv.api_id,
                            tv.name AS title,
                            tv.popularity,
                            tv.poster_path,
                            tv.vote_average,
                            CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                            'series' AS media_type,
                            s.seasons_count,
                            s.episodes_count,
                            CAST(NULL AS integer) AS runtime,
                            tv.first_air_date AS release_date
                        FROM tv_series tv
                        JOIN series_genre tsg ON tsg.series_id = tv.id
                        JOIN series_genres g ON g.id = tsg.genre_id
                        LEFT JOIN (
                            SELECT
                                stv.tv_series_id,
                                CAST(COUNT(*) AS integer) AS seasons_count,
                                CAST(COALESCE(SUM(stv.episode_count), 0) AS integer) AS episodes_count
                            FROM seasons stv
                            GROUP BY stv.tv_series_id
                        ) s ON s.tv_series_id = tv.id
                        WHERE LOWER(g.name) IN (:seriesGenres)
                    ) u
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN movie_genre mg ON mg.movie_id = m.id
                        JOIN movies_genres g ON g.id = mg.genre_id
                        WHERE LOWER(g.name) IN (:movieGenres)
                    
                        UNION ALL
                    
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN series_genre tsg ON tsg.series_id = tv.id
                        JOIN series_genres g ON g.id = tsg.genre_id
                        WHERE LOWER(g.name) IN (:seriesGenres)
                    ) u
                    """,
            nativeQuery = true
    )
    Page<MediaProjection> findMediaByGenres(
            @Param("movieGenres") List<String> movieGenres,
            @Param("seriesGenres") List<String> seriesGenres,
            Pageable pageable
    );

    @Query(
            value = """
                    SELECT DISTINCT
                        u.id AS id,
                        u.api_id AS apiId,
                        u.title AS title,
                        u.popularity AS popularity,
                        u.poster_path AS posterPath,
                        u.vote_average AS voteAverage,
                        u.year AS year,
                        u.media_type AS mediaType,
                        u.seasons_count AS seasonsCount,
                        u.episodes_count AS episodesCount,
                        u.runtime AS runtime,
                        u.release_date AS releaseDate
                    FROM (
                        SELECT
                            m.id,
                            m.api_id,
                            m.title,
                            m.popularity,
                            m.poster_path,
                            m.vote_average,
                            CAST(date_part('year', m.release_date) AS integer) AS year,
                            'movie' AS media_type,
                            CAST(NULL AS integer) AS seasons_count,
                            CAST(NULL AS integer) AS episodes_count,
                            m.runtime,
                            m.release_date AS release_date
                        FROM movies m
                        JOIN movie_genre mg ON mg.movie_id = m.id
                        JOIN movies_genres g ON g.id = mg.genre_id
                        WHERE LOWER(g.name) IN (:movieGenres)
                          AND m.release_date <= :startDate
                    
                        UNION ALL
                    
                        SELECT
                            tv.id,
                            tv.api_id,
                            tv.name AS title,
                            tv.popularity,
                            tv.poster_path,
                            tv.vote_average,
                            CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                            'series' AS media_type,
                            s.seasons_count,
                            s.episodes_count,
                            CAST(NULL AS integer) AS runtime,
                            tv.first_air_date AS release_date
                        FROM tv_series tv
                        JOIN series_genre tsg ON tsg.series_id = tv.id
                        JOIN series_genres g ON g.id = tsg.genre_id
                        LEFT JOIN (
                            SELECT
                                stv.tv_series_id,
                                CAST(COUNT(*) AS integer) AS seasons_count,
                                CAST(COALESCE(SUM(stv.episode_count), 0) AS integer) AS episodes_count
                            FROM seasons stv
                            GROUP BY stv.tv_series_id
                        ) s ON s.tv_series_id = tv.id
                        WHERE LOWER(g.name) IN (:seriesGenres)
                          AND tv.first_air_date <= :startDate
                    ) u
                    """,
            countQuery = """
                    SELECT COUNT(*)
                    FROM (
                        SELECT DISTINCT m.id
                        FROM movies m
                        JOIN movie_genre mg ON mg.movie_id = m.id
                        JOIN movies_genres g ON g.id = mg.genre_id
                        WHERE LOWER(g.name) IN (:movieGenres)
                          AND m.release_date <= :startDate
                    
                        UNION ALL
                    
                        SELECT DISTINCT tv.id
                        FROM tv_series tv
                        JOIN series_genre tsg ON tsg.series_id = tv.id
                        JOIN series_genres g ON g.id = tsg.genre_id
                        WHERE LOWER(g.name) IN (:seriesGenres)
                          AND tv.first_air_date <= :startDate
                    ) u
                    """,
            nativeQuery = true
    )
    Page<MediaProjection> findLatestMedia(@Param("startDate") LocalDate startDate, @Param("movieGenres") List<String> movieGenres, @Param("seriesGenres") List<String> seriesGenres, Pageable pageable);

    @Query(
            value = """
                SELECT DISTINCT
                    u.id               AS id,
                    u.api_id           AS apiId,
                    u.title            AS title,
                    u.popularity       AS popularity,
                    u.poster_path      AS posterPath,
                    u.vote_average     AS voteAverage,
                    u.year             AS year,
                    u.media_type       AS mediaType,
                    u.seasons_count    AS seasonsCount,
                    u.episodes_count   AS episodesCount,
                    u.runtime          AS runtime,
                    u.release_date     AS releaseDate,
                    u.vote_count       AS voteCount,
                    u.trailer          AS trailer,
                    u.genre            AS genre
                FROM (
                    SELECT
                        m.id,
                        m.api_id,
                        m.title,
                        m.popularity,
                        m.poster_path,
                        m.vote_average,
                        CAST(date_part('year', m.release_date) AS integer) AS year,
                        'movie' AS media_type,
                        CAST(NULL AS integer) AS seasons_count,
                        CAST(NULL AS integer) AS episodes_count,
                        m.runtime,
                        m.release_date AS release_date,
                        m.vote_count AS vote_count,
                        m.trailer AS trailer,
                        MIN(g.name) AS genre
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:movieGenres)
                    GROUP BY
                        m.id,
                        m.api_id,
                        m.title,
                        m.popularity,
                        m.poster_path,
                        m.vote_average,
                        m.runtime,
                        m.release_date,
                        m.vote_count,
                        m.trailer

                    UNION ALL

                    SELECT
                        tv.id,
                        tv.api_id,
                        tv.name AS title,
                        tv.popularity,
                        tv.poster_path,
                        tv.vote_average,
                        CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                        'series' AS media_type,
                        s.seasons_count,
                        s.episodes_count,
                        CAST(NULL AS integer) AS runtime,
                        tv.first_air_date AS release_date,
                        tv.vote_count AS vote_count,
                        tv.trailer AS trailer,
                        MIN(g.name) AS genre
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    LEFT JOIN (
                        SELECT
                            stv.tv_series_id,
                            CAST(COUNT(*) AS integer) AS seasons_count,
                            CAST(COALESCE(SUM(stv.episode_count), 0) AS integer) AS episodes_count
                        FROM seasons stv
                        GROUP BY stv.tv_series_id
                    ) s ON s.tv_series_id = tv.id
                    WHERE LOWER(g.name) IN (:seriesGenres)
                    GROUP BY
                        tv.id,
                        tv.api_id,
                        tv.name,
                        tv.popularity,
                        tv.poster_path,
                        tv.vote_average,
                        s.seasons_count,
                        s.episodes_count,
                        tv.first_air_date,
                        tv.vote_count,
                        tv.trailer
                ) u
                """,
            countQuery = """
                SELECT COUNT(*)
                FROM (
                    SELECT DISTINCT m.id
                    FROM movies m
                    JOIN movie_genre mg ON mg.movie_id = m.id
                    JOIN movies_genres g ON g.id = mg.genre_id
                    WHERE LOWER(g.name) IN (:movieGenres)

                    UNION ALL

                    SELECT DISTINCT tv.id
                    FROM tv_series tv
                    JOIN series_genre tsg ON tsg.series_id = tv.id
                    JOIN series_genres g ON g.id = tsg.genre_id
                    WHERE LOWER(g.name) IN (:seriesGenres)
                ) u
                """,
            nativeQuery = true
    )
    Page<MediaWithGenreProjection> findAllByGenresMapped(@Param("movieGenres") List<String> movieGenres, @Param("seriesGenres") List<String> seriesGenres, Pageable pageable);
}
