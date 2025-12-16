package com.moviefy.database.repository.credit.crew;

import com.moviefy.database.model.entity.credit.crew.CrewMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface CrewMovieRepository extends JpaRepository<CrewMovie, Long> {
    @Query("SELECT cm FROM CrewMovie cm WHERE cm.movie.id = :movieId AND cm.crew.apiId = :crewApiId AND cm.job.job = :job")
    Optional<CrewMovie> findByMovieIdAndCrewApiIdAndJobJob(@Param("movieId") Long movieId, @Param("crewApiId") Long crewApiId, @Param("job") String job);

    List<CrewMovie> findCrewByMovieId(Long movieId);

    @Query("SELECT cm.crew.id FROM CrewMovie cm WHERE cm.movie.id =:movieId")
    Set<Long> findCrewIdsByMovieId(@Param("movieId") Long movieId);

    void deleteByMovieId(long id);
}
