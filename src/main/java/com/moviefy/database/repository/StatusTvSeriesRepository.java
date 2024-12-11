package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.StatusTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTvSeriesRepository extends JpaRepository<StatusTvSeries, Long> {
    Optional<StatusTvSeries> findByStatus(String status);
}
