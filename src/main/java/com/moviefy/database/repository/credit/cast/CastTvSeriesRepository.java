package com.moviefy.database.repository.credit.cast;

import com.moviefy.database.model.entity.credit.cast.CastTvSeries;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CastTvSeriesRepository extends JpaRepository<CastTvSeries, Long> {
    @Query("SELECT cm FROM CastTvSeries cm WHERE cm.tvSeries.id = :tvSeriesId AND cm.cast.apiId = :crewApiId AND cm.character = :character")
    Optional<CastTvSeries> findByTvSeriesIdAndCastApiIdAndCharacter(@Param("tvSeriesId") Long tvSeriesId, @Param("crewApiId") Long crewApiId, @Param("character") String character);

    List<CastTvSeries> findCastByTvSeriesId(Long tvId);

    void deleteByTvSeriesId(long id);

    @Query("SELECT cts.cast.id FROM CastTvSeries cts WHERE cts.tvSeries.id =:tvSeriesId")
    Set<Long> findCastIdsByTvSeriesId(@Param("tvSeriesId") long tvSeriesId);
}
