package com.moviefy.database.repository.media.tvSeries;

import com.moviefy.database.model.entity.media.tvSeries.StatusTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StatusTvSeriesRepository extends JpaRepository<StatusTvSeries, Long> {
    Optional<StatusTvSeries> findByStatus(String status);
}
