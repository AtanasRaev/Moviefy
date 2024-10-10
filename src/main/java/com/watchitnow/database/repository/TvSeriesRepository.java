package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.TvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface TvSeriesRepository extends JpaRepository<TvSeries, Long> {
    @Query("SELECT COUNT(tv) FROM TvSeries tv WHERE EXTRACT(YEAR FROM tv.firstAirDate) = :year AND EXTRACT(MONTH FROM tv.firstAirDate) = :month")
    long countTvSeriesInDateRange(@Param("year") int year, @Param("month") int month);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.firstAirDate = (SELECT MIN(tv2.firstAirDate) FROM TvSeries tv2)")
    List<TvSeries> findOldestTvSeries();

    Optional<TvSeries> findByApiId(Long id);

    TvSeries findTopByOrderByIdDesc();

    @Query("SELECT DISTINCT tv FROM TvSeries tv LEFT JOIN FETCH tv.genres WHERE tv.firstAirDate BETWEEN :startDate AND :endDate")
    List<TvSeries> findByFirstAirDateBetweenWithGenres(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);
}
