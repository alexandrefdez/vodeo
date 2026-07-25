package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.model.Series;
import com.tfg.streaming.catalog_service.repository.SeriesRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SeriesRepositoryTest {

    @Autowired
    private SeriesRepository seriesRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByIdIn - hidrata solo las series pedidas para la Composicion de API (Watchlist)")
    void findByIdIn_devuelveSoloLasSeriesPedidas() {
        Series serie1 = new Series();
        serie1.setTitle("Serie 1");
        entityManager.persistAndFlush(serie1);

        Series serie2 = new Series();
        serie2.setTitle("Serie 2");
        entityManager.persistAndFlush(serie2);

        Series serie3 = new Series();
        serie3.setTitle("Serie 3 (no pedida)");
        entityManager.persistAndFlush(serie3);

        List<Series> encontradas = seriesRepository.findByIdIn(List.of(serie1.getId(), serie2.getId()));

        assertEquals(2, encontradas.size());
        assertTrue(encontradas.stream().noneMatch(s -> s.getId().equals(serie3.getId())));
    }
}