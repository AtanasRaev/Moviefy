package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.genre.MovieGenre;
import com.watchitnow.database.model.entity.media.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    Optional<MovieGenre> findByApiId(Long genre);

    Optional<MovieGenre> findByName(String genreType);

    @Query("SELECT mg FROM MovieGenre mg JOIN mg.movies m WHERE m.id = :movieId")
    Set<MovieGenre> findByMovieId(@Param("movieId") Long movieId);
}
