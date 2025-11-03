package com.moviefy.database.repository.media;

import com.moviefy.database.model.dto.pageDto.CombinedMediaProjection;
import com.moviefy.database.model.entity.media.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CombinedMediaRepository extends JpaRepository<Movie, Long> {

    @Query(value = """
            SELECT u.id,
                   u.title,
                   u.popularity,
                   u.poster_path AS posterPath,
                   u.vote_average AS voteAverage,
                   u.year,
                   u.type,
                   u.seasons_count AS seasonsCount,
                   u.episodes_count AS episodesCount
            FROM (
                SELECT m.id,
                       m.title,
                       m.popularity,
                       m.poster_path,
                       m.vote_average,
                       CAST(date_part('year', m.release_date) AS integer) AS year,
                       'movie' AS type,
                       CAST(NULL AS integer) AS seasons_count,
                       CAST(NULL AS integer) AS episodes_count
                FROM movies m
                JOIN movie_genre mg ON mg.movie_id = m.id
                JOIN movies_genres g ON g.id = mg.genre_id
                WHERE g.name IN (:movieGenres)
            
                UNION ALL
            
                SELECT tv.id,
                       tv.name AS title,
                       tv.popularity,
                       tv.poster_path,
                       tv.vote_average,
                       CAST(date_part('year', tv.first_air_date) AS integer) AS year,
                       'series' AS type,
                       s.seasons_count,
                       s.episodes_count
                FROM tv_series tv
                JOIN series_genre tsg ON tsg.series_id = tv.id
                JOIN series_genres g ON g.id = tsg.genre_id
                LEFT JOIN (
                    SELECT stv.tv_series_id,
                           CAST(COUNT(*) AS integer) AS seasons_count,
                           CAST(COALESCE(SUM(stv.episode_count), 0) AS integer) AS episodes_count
                    FROM seasons stv
                    GROUP BY stv.tv_series_id
                ) s ON s.tv_series_id = tv.id
                WHERE g.name IN (:seriesGenres)
            ) u
            ORDER BY u.popularity DESC, u.id
            LIMIT :limit OFFSET :offset
            """, nativeQuery = true)
    List<CombinedMediaProjection> findCombinedByGenres(
            @Param("movieGenres") List<String> movieGenres,
            @Param("seriesGenres") List<String> seriesGenres,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    @Query(value = """
            SELECT COUNT(*) FROM (
                SELECT m.id
                FROM movies m
                JOIN movie_genre mg ON mg.movie_id = m.id
                JOIN movies_genres g ON g.id = mg.genre_id
                WHERE g.name IN (:movieGenres)
                
                UNION ALL
                
                SELECT tv.id
                FROM tv_series tv
                JOIN series_genre tsg ON tsg.series_id = tv.id
                JOIN series_genres g ON g.id = tsg.genre_id
                WHERE g.name IN (:seriesGenres)
            ) u
            """, nativeQuery = true)
    long countCombinedByGenres(
            @Param("movieGenres") List<String> movieGenres,
            @Param("seriesGenres") List<String> seriesGenres
    );
}
