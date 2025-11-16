package com.moviefy.database.repository.genre;

import com.moviefy.database.model.entity.genre.MovieGenre;
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

    @Query("SELECT mg.name FROM MovieGenre mg")
    List<String> findAllNames();
}
