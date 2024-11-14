package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Cast.CastTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CastTvSeriesRepository extends JpaRepository<CastTvSeries, Long> {
    Optional<CastTvSeries> findByTvSeriesIdAndCastIdAndCharacter(long id, long id1, String character);
}
