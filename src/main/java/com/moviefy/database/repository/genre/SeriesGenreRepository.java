package com.moviefy.database.repository.genre;

import com.moviefy.database.model.entity.genre.SeriesGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface SeriesGenreRepository extends JpaRepository<SeriesGenre, Long> {
    @Query("SELECT sg FROM SeriesGenre sg WHERE sg.apiId IN :genresApiIds")
    Set<SeriesGenre> findAllByApiId(@Param("genresApiIds") Set<Long> genresApiIds);

    Optional<SeriesGenre> findByName(String genreType);

    @Query("SELECT sg FROM SeriesGenre sg JOIN sg.tvSeries tv WHERE tv.id = :tvSeriesId")
    Set<SeriesGenre> findByTvSeriesId(@Param("tvSeriesId") Long tvSeriesId);

    @Query("SELECT sg.name FROM SeriesGenre sg")
    List<String> findAllNames();
}
