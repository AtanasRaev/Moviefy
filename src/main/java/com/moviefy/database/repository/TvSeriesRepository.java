package com.moviefy.database.repository;

import com.moviefy.database.model.dto.pageDto.tvSeriesDto.TvSeriesTrendingPageDTO;
import com.moviefy.database.model.entity.media.TvSeries;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query("""
            SELECT tv FROM TvSeries tv
            WHERE LOWER(tv.name) LIKE LOWER(CONCAT('%', :query, '%'))
               OR LOWER(tv.originalName) LIKE LOWER(CONCAT('%', :query, '%'))
            ORDER BY
               CASE WHEN LOWER(tv.name) = LOWER(:query) THEN 0
                    WHEN LOWER(tv.name) LIKE LOWER(CONCAT(:query, '%')) THEN 1
                    ELSE 2 END,
               tv.name
            """)
    Page<TvSeries> searchByName(@Param("query") String query, Pageable pageable);
}
