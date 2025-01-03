package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.SeasonTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface SeasonTvSeriesRepository extends JpaRepository<SeasonTvSeries, Long> {
    Optional<SeasonTvSeries> findByApiId(Long id);

    List<SeasonTvSeries> findAllBySeasonNumber(int seasonNumber);

    List<SeasonTvSeries> findAllByTvSeriesId(Long tvSeriesId);

    @Query("SELECT s FROM SeasonTvSeries s LEFT JOIN FETCH s.tvSeries tv WHERE EXTRACT(YEAR FROM s.airDate) BETWEEN :startYear AND :endYear" +
            " AND tv.voteCount > 200" +
            " AND EXTRACT(YEAR FROM tv.firstAirDate) BETWEEN 2015 and :endYear")
    List<SeasonTvSeries> findAllByYearRange(int startYear, int endYear);
}
