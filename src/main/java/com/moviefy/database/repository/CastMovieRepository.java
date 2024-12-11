package com.moviefy.database.repository;

import com.moviefy.database.model.entity.credit.cast.CastMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CastMovieRepository extends JpaRepository<CastMovie, Long> {
    @Query("SELECT cm FROM CastMovie cm WHERE cm.movie.id = :movieId AND cm.cast.apiId = :crewApiId AND cm.character = :character")
    Optional<CastMovie> findByMovieIdAndCastApiIdAndCharacter(@Param("movieId") Long movieId, @Param("crewApiId") Long crewApiId, @Param("character") String character);

    List<CastMovie> findCastByMovieId(Long movieId);
}
