package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.model.Episode;
import com.tfg.streaming.catalog_service.model.Season;
import com.tfg.streaming.catalog_service.model.Series;
import com.tfg.streaming.catalog_service.repository.EpisodeRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class EpisodeRepositoryTest {

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private TestEntityManager entityManager;

    private Season crearTemporada(Series series, int numero) {
        Season season = new Season();
        season.setSeasonNumber(numero);
        season.setSeries(series);
        return entityManager.persistAndFlush(season);
    }

    private Episode crearEpisodio(Season season, int numero) {
        Episode episode = new Episode();
        episode.setEpisodeNumber(numero);
        episode.setTitle("Episodio " + numero);
        episode.setSeason(season);
        return entityManager.persistAndFlush(episode);
    }

    @Test
    @DisplayName("findFirstBySeasonIdAndEpisodeNumberGreaterThan - encuentra el siguiente episodio de la misma temporada")
    void siguienteEpisodio_mismaTemporada_loEncuentra() {
        Series series = new Series();
        series.setTitle("Serie de prueba");
        entityManager.persistAndFlush(series);

        Season season = crearTemporada(series, 1);
        crearEpisodio(season, 1);
        Episode episodio2 = crearEpisodio(season, 2);

        Optional<Episode> siguiente = episodeRepository
                .findFirstBySeasonIdAndEpisodeNumberGreaterThanOrderByEpisodeNumberAsc(season.getId(), 1);

        assertTrue(siguiente.isPresent());
        assertEquals(episodio2.getId(), siguiente.get().getId());
    }

    @Test
    @DisplayName("findFirstBySeasonIdAndEpisodeNumberGreaterThan - ultimo episodio de la temporada no tiene siguiente")
    void siguienteEpisodio_ultimoDeLaTemporada_devuelveVacio() {
        Series series = new Series();
        series.setTitle("Serie de prueba");
        entityManager.persistAndFlush(series);

        Season season = crearTemporada(series, 1);
        crearEpisodio(season, 1);

        Optional<Episode> siguiente = episodeRepository
                .findFirstBySeasonIdAndEpisodeNumberGreaterThanOrderByEpisodeNumberAsc(season.getId(), 1);

        assertTrue(siguiente.isEmpty());
    }
}