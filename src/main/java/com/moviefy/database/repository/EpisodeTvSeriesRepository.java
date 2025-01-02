package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.EpisodeTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EpisodeTvSeriesRepository extends JpaRepository<EpisodeTvSeries, Long> {
}
