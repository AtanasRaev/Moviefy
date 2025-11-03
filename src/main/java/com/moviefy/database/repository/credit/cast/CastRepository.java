package com.moviefy.database.repository.credit.cast;

import com.moviefy.database.model.entity.credit.cast.Cast;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CastRepository extends JpaRepository<Cast, Long> {
    @Query("SELECT c FROM Cast c WHERE c.apiId IN :apiIds")
    List<Cast> findAllByApiIds(@Param("apiIds") List<Long> apiIds);
}
