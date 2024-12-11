package com.moviefy.database.repository;

import com.moviefy.database.model.entity.credit.crew.JobCrew;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface JobCrewRepository extends JpaRepository<JobCrew, Long> {
    Optional<JobCrew> findByJob(String name);
}
