package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.crew.Crew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CrewRepository extends JpaRepository<Crew, Long> {
    @Query("SELECT c FROM Crew c WHERE c.apiId IN :apiIds")
    List<Crew> findAllByApiIds(@Param("apiIds") List<Long> apiIds);
}
