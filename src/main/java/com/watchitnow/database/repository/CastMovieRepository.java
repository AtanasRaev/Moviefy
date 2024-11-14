package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Cast.CastMovie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CastMovieRepository extends JpaRepository<CastMovie, Long> {
}
