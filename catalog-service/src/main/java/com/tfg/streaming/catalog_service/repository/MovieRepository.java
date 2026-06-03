package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Movie;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MovieRepository extends JpaRepository<Movie, Long> {

    @EntityGraph(attributePaths = { "genres" })
    Page<Movie> findAll(Pageable pageable);

    @EntityGraph(attributePaths = { "genres" })
    Optional<Movie> findById(Long id);

    @EntityGraph(attributePaths = { "genres" })
    Page<Movie> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    // Metodos para filtrar por genero y watchlist
    @EntityGraph(attributePaths = { "genres" })
    Page<Movie> findByGenresName(String genreName, Pageable pageable);

    @EntityGraph(attributePaths = { "genres" })
    List<Movie> findByIdIn(List<Long> ids);
}