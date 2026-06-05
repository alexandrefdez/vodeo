package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    List<Season> findBySeriesIdOrderBySeasonNumberAsc(Long seriesId);

    // Busca la siguiente temporada disponible de una serie
    Optional<Season> findFirstBySeriesIdAndSeasonNumberGreaterThanOrderBySeasonNumberAsc(Long seriesId,
            Integer seasonNumber);
}