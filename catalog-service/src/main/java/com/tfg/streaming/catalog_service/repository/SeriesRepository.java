package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Series;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SeriesRepository extends JpaRepository<Series, Long> {

    @EntityGraph(attributePaths = { "genres" })
    Page<Series> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "genres" })
    Optional<Series> findById(Long id);

    @EntityGraph(attributePaths = { "genres" })
    Page<Series> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    @EntityGraph(attributePaths = { "genres" })
    Page<Series> findByGenresName(String genreName, Pageable pageable);

    @EntityGraph(attributePaths = { "genres" })
    List<Series> findByIdIn(List<Long> ids);
}