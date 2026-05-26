package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Movie;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @EntityGraph(attributePaths = { "genres" })
    List<Movie> findAll();

    @EntityGraph(attributePaths = { "genres" })
    Optional<Movie> findById(Long id);

    @EntityGraph(attributePaths = { "genres" })
    List<Movie> findByTitleContainingIgnoreCase(String title);
}