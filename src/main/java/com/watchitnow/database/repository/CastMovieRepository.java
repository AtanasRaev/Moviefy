package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Cast.Cast;
import com.watchitnow.database.model.entity.credit.Cast.CastMovie;
import com.watchitnow.database.model.entity.media.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CastMovieRepository extends JpaRepository<CastMovie, Long> {
//    Optional<CastMovie> findByMovieAndCastAndCharacter(Movie movie, Cast cast, String character);
    Optional<CastMovie> findByMovieIdAndCastIdAndCharacter(Long movieId, Long castId, String character);



}
