package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.Movie;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    Optional<Movie> findByApiId(Long apiId);

    @Query("SELECT COUNT(m) FROM Movie m WHERE EXTRACT(YEAR FROM m.releaseDate) = :year AND EXTRACT(MONTH FROM m.releaseDate) = :month")
    long countMoviesInDateRange(@Param("year") int year, @Param("month") int month);

    @Query("SELECT m FROM Movie m WHERE m.releaseDate = (SELECT MIN(m2.releaseDate) FROM Movie m2)")
    List<Movie> findOldestMovie();

    @Query("SELECT DISTINCT m FROM Movie m LEFT JOIN FETCH m.genres LEFT JOIN FETCH m.productionCompanies WHERE m.id = :id")
    Optional<Movie> findMovieById(@Param("id") Long id);

    @Query("SELECT DISTINCT m FROM Movie m JOIN FETCH m.genres WHERE m.releaseDate BETWEEN :startDate AND :endDate")
    List<Movie> findByReleaseDateBetweenWithGenres(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    List<Movie> findAllByPosterPathIsNull();

    @Query("SELECT m FROM Movie m LEFT JOIN FETCH m.genres g WHERE g.name = :genreName")
    List<Movie> findByGenreName(@Param("genreName") String genreName);

}
