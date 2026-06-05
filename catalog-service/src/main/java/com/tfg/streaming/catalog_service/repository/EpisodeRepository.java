package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Episode;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EpisodeRepository extends JpaRepository<Episode, Long> {

    // Busca el siguiente episodio dentro de la MISMA temporada
    Optional<Episode> findFirstBySeasonIdAndEpisodeNumberGreaterThanOrderByEpisodeNumberAsc(Long seasonId,
            Integer episodeNumber);

    // Busca el PRIMER episodio de una temporada nueva
    Optional<Episode> findFirstBySeasonIdOrderByEpisodeNumberAsc(Long seasonId);

}