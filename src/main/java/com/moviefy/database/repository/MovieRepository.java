package com.moviefy.database.repository;

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

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {
    Optional<Movie> findByApiId(Long apiId);

    @Query("SELECT COUNT(m) FROM Movie m WHERE EXTRACT(YEAR FROM m.releaseDate) = " +
            "(SELECT MAX(EXTRACT(YEAR FROM m.releaseDate)) FROM Movie m)")
    Long countNewestMovies();

    @Query("SELECT MAX(EXTRACT(YEAR FROM m.releaseDate)) FROM Movie m")
    Integer findNewestMovieYear();

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres LEFT JOIN FETCH m.productionCompanies WHERE m.id = :id")
    Optional<Movie> findMovieById(@Param("id") Long id);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate <= :startDate ORDER BY m.releaseDate DESC, m.id")
    Page<Movie> findByReleaseDate(@Param("startDate") LocalDate startDate, Pageable pageable);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.genres g WHERE g.name = :genreName")
    List<Movie> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT m FROM Movie m ORDER BY m.popularity DESC")
    Page<Movie> findAllByPopularityDesc(Pageable pageable);

    @Query("SELECT m FROM Movie m")
    Page<Movie> findAllSortedByVoteCount(Pageable pageable);

    @Query("""
            SELECT m FROM Movie m
            WHERE LOWER(m.title) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(m.originalTitle) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY
               CASE WHEN LOWER(m.title) = LOWER(:query) THEN 0
                    WHEN LOWER(m.title) LIKE LOWER(CONCAT(:query, '%')) THEN 1
                    ELSE 2 END,
               m.title
            """)
    Page<Movie> searchByTitle(@Param("query") String query, Pageable pageable);
}
