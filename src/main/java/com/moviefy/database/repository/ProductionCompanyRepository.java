package com.moviefy.database.repository;

import com.moviefy.database.model.entity.ProductionCompany;
import com.moviefy.database.model.entity.media.Movie;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductionCompanyRepository extends JpaRepository<ProductionCompany, Long> {
    Optional<ProductionCompany> findByApiId(Long id);
}
