package com.moviefy.database.repository;

import com.moviefy.database.model.entity.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Optional<Collection> findByApiId(Long apiId);
}
