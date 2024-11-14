package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Crew.CrewTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrewTvSeriesRepository extends JpaRepository<CrewTvSeries, Long> {
    @Query("SELECT cm FROM CrewTvSeries cm WHERE cm.tvSeries.id = :tvSeriesId AND cm.crew.id = :crewId AND cm.job.job = :job")
    Optional<CrewTvSeries> findByTvSeriesIdAndCrewIdAndJobJob(@Param("tvSeriesId") Long tvSeriesId, @Param("crewId") Long crewId, @Param("job") String job);
}
