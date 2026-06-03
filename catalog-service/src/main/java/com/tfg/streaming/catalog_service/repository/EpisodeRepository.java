package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Episode;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {
}