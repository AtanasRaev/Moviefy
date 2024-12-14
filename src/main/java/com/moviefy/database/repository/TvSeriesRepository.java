package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.TvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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
            "WHERE tv.id = :id")
    Optional<TvSeries> findTvSeriesById(@Param("id") Long id);

    @Query("SELECT DISTINCT tv FROM TvSeries tv LEFT JOIN FETCH tv.genres LEFT JOIN FETCH tv.seasons WHERE tv.firstAirDate BETWEEN :startDate AND :endDate")
    List<TvSeries> findByFirstAirDateBetweenWithGenres(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    @Query("SELECT tv FROM TvSeries tv ORDER BY tv.popularity DESC LIMIT :totalItems")
    List<TvSeries> findAllSortedByPopularity(@Param("totalItems") int totalItems);

    @Query("SELECT tv FROM TvSeries tv LEFT JOIN FETCH tv.genres g WHERE g.name = :genreName")
    List<TvSeries> findByGenreName(@Param("genreName") String genreName);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.voteCount IS NOT NULL ORDER BY tv.voteCount DESC LIMIT :totalItems")
    List<TvSeries> findAllSortedByVoteCount(@Param("totalItems") int totalItems);
}
