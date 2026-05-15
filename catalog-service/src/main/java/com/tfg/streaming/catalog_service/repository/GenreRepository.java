package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GenreRepository extends JpaRepository<Genre, Long> {
    boolean existsByName(String name);
}