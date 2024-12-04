package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Cast.CastTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CastTvSeriesRepository extends JpaRepository<CastTvSeries, Long> {
    @Query("SELECT cm FROM CastTvSeries cm WHERE cm.tvSeries.id = :tvSeriesId AND cm.cast.apiId = :crewApiId AND cm.character = :character")
    Optional<CastTvSeries> findByTvSeriesIdAndCastApiIdAndCharacter(@Param("tvSeriesId") Long tvSeriesId, @Param("crewApiId") Long crewApiId, @Param("character") String character);

    List<CastTvSeries> findCastByTvSeriesId(Long tvId);
}
