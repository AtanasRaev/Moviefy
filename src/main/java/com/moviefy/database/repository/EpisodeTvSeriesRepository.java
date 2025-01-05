package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.EpisodeTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EpisodeTvSeriesRepository extends JpaRepository<EpisodeTvSeries, Long> {
    @Query("SELECT e FROM EpisodeTvSeries e WHERE e.season.id = :seasonId")
    List<EpisodeTvSeries> findAllBySeasonId(@Param("seasonId") Long seasonId);
}
