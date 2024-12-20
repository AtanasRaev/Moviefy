package com.moviefy.database.repository;

import com.moviefy.database.model.entity.media.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    Optional<Collection> findByApiId(Long apiId);

    @Query("SELECT c FROM Collection c WHERE c.name ILIKE :name")
    List<Collection> findByName(@Param("name") String name);

    @Query("SELECT c FROM Collection c WHERE c.name IN :names")
    List<Collection> findAllByNameIn(List<String> names);

    @Query("SELECT c FROM Collection c JOIN c.movies m WHERE m.id = :movieId")
    Optional<Collection> findCollectionsByMovieId(@Param("movieId") Long movieId);
}
