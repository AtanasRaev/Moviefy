package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Cast.CastMovie;
import com.watchitnow.database.model.entity.credit.Crew.CrewTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CrewTvSeriesRepository extends JpaRepository<CrewTvSeries, Long> {
    @Query("SELECT cm FROM CrewTvSeries cm WHERE cm.tvSeries.id = :tvSeriesId AND cm.crew.apiId = :crewApiId AND cm.job.job = :job")
    Optional<CrewTvSeries> findByTvSeriesIdAndCrewApiIdAndJobJob(@Param("tvSeriesId") Long tvSeriesId, @Param("crewApiId") Long crewApiId, @Param("job") String job);

    List<CrewTvSeries> findCrewByTvSeriesId(Long movieId);

}
