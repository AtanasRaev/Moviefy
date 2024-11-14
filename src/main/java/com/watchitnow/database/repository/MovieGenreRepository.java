package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.genre.MovieGenre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MovieGenreRepository extends JpaRepository<MovieGenre, Long> {
    Optional<MovieGenre> findByApiId(Long genre);

    Optional<MovieGenre> findByName(String genreType);
}
