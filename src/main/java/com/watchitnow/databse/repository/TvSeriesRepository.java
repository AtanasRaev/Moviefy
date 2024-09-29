package com.watchitnow.databse.repository;

import com.watchitnow.databse.model.entity.TvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface TvSeriesRepository extends JpaRepository<TvSeries, Long> {
    Optional<TvSeries> findTopByOrderByIdDesc();

    @Query("SELECT s FROM TvSeries s WHERE EXTRACT(YEAR FROM s.firstAirDate) = :year AND EXTRACT(MONTH FROM s.firstAirDate) = :month")
    List<TvSeries> findMoviesByYearAndMonth(@Param("year") int year, @Param("month") int month);

    @Query("SELECT COUNT(tv) FROM TvSeries tv WHERE EXTRACT(YEAR FROM tv.firstAirDate) = :year AND EXTRACT(MONTH FROM tv.firstAirDate) = :month")
    long countTvSeriesInDateRange(@Param("year") int year, @Param("month") int month);

    @Query("SELECT tv FROM TvSeries tv WHERE tv.firstAirDate = (SELECT MIN(tv2.firstAirDate) FROM TvSeries tv2)")
    List<TvSeries> findOldestTvSeries();

    Optional<TvSeries> findByApiId(Long id);
}
