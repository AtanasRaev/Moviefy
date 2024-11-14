package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Crew.CrewMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CrewMovieRepository extends JpaRepository<CrewMovie, Long> {
    @Query("SELECT cm FROM CrewMovie cm WHERE cm.movie.id = :movieId AND cm.crew.id = :crewId AND cm.job.job = :job")
    Optional<CrewMovie> findByMovieIdAndCrewIdAndJobJob(@Param("movieId") Long movieId, @Param("crewId") Long crewId, @Param("job") String job);
}
