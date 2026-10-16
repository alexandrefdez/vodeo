package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.model.Season;
import com.tfg.streaming.catalog_service.model.Series;
import com.tfg.streaming.catalog_service.repository.SeasonRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SeasonRepositoryTest {

    @Autowired
    private SeasonRepository seasonRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findFirstBySeriesIdAndSeasonNumberGreaterThan - encuentra la siguiente temporada al cruzar el limite")
    void siguienteTemporada_existente_laEncuentra() {
        Series series = new Series();
        series.setTitle("Serie de prueba");
        entityManager.persistAndFlush(series);

        Season temporada1 = new Season();
        temporada1.setSeasonNumber(1);
        temporada1.setSeries(series);
        entityManager.persistAndFlush(temporada1);

        Season temporada2 = new Season();
        temporada2.setSeasonNumber(2);
        temporada2.setSeries(series);
        entityManager.persistAndFlush(temporada2);

        Optional<Season> siguiente = seasonRepository
                .findFirstBySeriesIdAndSeasonNumberGreaterThanOrderBySeasonNumberAsc(series.getId(), 1);

        assertTrue(siguiente.isPresent());
        assertEquals(temporada2.getId(), siguiente.get().getId());
    }

    @Test
    @DisplayName("findFirstBySeriesIdAndSeasonNumberGreaterThan - ultima temporada de la serie no tiene siguiente")
    void siguienteTemporada_ultimaDeLaSerie_devuelveVacio() {
        Series series = new Series();
        series.setTitle("Serie de prueba");
        entityManager.persistAndFlush(series);

        Season unicaTemporada = new Season();
        unicaTemporada.setSeasonNumber(1);
        unicaTemporada.setSeries(series);
        entityManager.persistAndFlush(unicaTemporada);

        Optional<Season> siguiente = seasonRepository
                .findFirstBySeriesIdAndSeasonNumberGreaterThanOrderBySeasonNumberAsc(series.getId(), 1);

        assertTrue(siguiente.isEmpty());
    }
}