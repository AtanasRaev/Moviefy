package com.watchitnow.database.repository;

import com.watchitnow.database.model.entity.ProductionCompany;
import com.watchitnow.service.impl.MovieServiceImpl;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductionCompanyRepository extends JpaRepository<ProductionCompany, Long> {
    Optional<ProductionCompany> findByApiId(Long id);
}
