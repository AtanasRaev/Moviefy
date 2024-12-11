package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.SeasonTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SeasonTvSeriesRepository extends JpaRepository<SeasonTvSeries, Long> {
    Optional<SeasonTvSeries> findByApiId(Long id);

    List<SeasonTvSeries> findAllBySeasonNumber(int seasonNumber);

    List<SeasonTvSeries> findAllByTvSeriesId(Long tvSeriesId);
}
