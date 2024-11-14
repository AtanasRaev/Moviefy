package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Cast.Cast;
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
