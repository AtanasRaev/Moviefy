package com.moviefy.database.repository.media.tvSeries;

import com.moviefy.database.model.entity.media.tvSeries.SeasonTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SeasonTvSeriesRepository extends JpaRepository<SeasonTvSeries, Long> {
    Optional<SeasonTvSeries> findByApiId(Long id);

    Set<SeasonTvSeries> findAllByTvSeriesId(Long tvSeriesId);
}
