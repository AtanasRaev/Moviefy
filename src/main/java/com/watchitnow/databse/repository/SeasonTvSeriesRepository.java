package com.watchitnow.databse.repository;

import com.watchitnow.databse.model.entity.SeasonTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeasonTvSeriesRepository extends JpaRepository<SeasonTvSeries, Long> {
    Optional<SeasonTvSeries> findByApiId(Long id);
}
