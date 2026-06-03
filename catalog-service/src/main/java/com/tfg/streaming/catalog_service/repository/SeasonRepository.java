package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Season;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface SeasonRepository extends JpaRepository<Season, Long> {
    List<Season> findBySeriesIdOrderBySeasonNumberAsc(Long seriesId);
}