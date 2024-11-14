package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Crew.CrewMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CrewMovieRepository extends JpaRepository<CrewMovie, Long> {
}
