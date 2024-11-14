package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.credit.Crew.JobCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobCrewRepository extends JpaRepository<JobCrew, Long> {
    Optional<JobCrew> findByJob(String name);
}
