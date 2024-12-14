package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.Movie;
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

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.releaseDate BETWEEN :startDate AND :endDate")
    List<Movie> findByReleaseDateBetweenWithGenres(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.genres g WHERE g.name = :genreName")
    List<Movie> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT m FROM Movie m ORDER BY m.popularity DESC LIMIT :totalItems")
    List<Movie> findAllByPopularityDesc(@Param("totalItems") int totalItems);

    @Query("SELECT m FROM Movie m WHERE m.voteCount IS NOT NULL ORDER BY m.voteCount DESC LIMIT :totalItems")
    List<Movie> findAllSortedByVoteCount(@Param("totalItems") int totalItems);
}
