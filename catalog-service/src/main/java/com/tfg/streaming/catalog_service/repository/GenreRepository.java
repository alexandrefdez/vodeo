package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    Optional<Genre> findByName(String name);

    @Query("SELECT DISTINCT g.name FROM Genre g JOIN g.movies m")
    List<String> findActiveGenres();
}