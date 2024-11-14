package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Cast.CastTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastTvSeriesRepository extends JpaRepository<CastTvSeries, Long> {
}
