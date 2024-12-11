package com.moviefy.database.repository;

import com.moviefy.database.model.entity.genre.SeriesGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface SeriesGenreRepository extends JpaRepository<SeriesGenre, Long> {
    Optional<SeriesGenre> findByApiId(Long genre);

    Optional<SeriesGenre> findByName(String genreType);

    @Query("SELECT sg FROM SeriesGenre sg JOIN sg.tvSeries tv WHERE tv.id = :tvSeriesId")
    Set<SeriesGenre> findByTvSeriesId(@Param("tvSeriesId") Long tvSeriesId);
}
