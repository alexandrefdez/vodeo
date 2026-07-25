package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.model.Episode;
import com.tfg.streaming.catalog_service.model.Season;
import com.tfg.streaming.catalog_service.model.Series;
import com.tfg.streaming.catalog_service.model.Subtitle;
import com.tfg.streaming.catalog_service.repository.EpisodeRepository;
import com.tfg.streaming.catalog_service.repository.SubtitleRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class SubtitleRepositoryTest {

    @Autowired
    private SubtitleRepository subtitleRepository;

    @Autowired
    private EpisodeRepository episodeRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("borrar un episodio borra en cascada sus subtitulos (orphanRemoval)")
    void borrarEpisodio_borraSusSubtitulosEnCascada() {
        Series series = new Series();
        series.setTitle("Serie de prueba");
        entityManager.persistAndFlush(series);

        Season season = new Season();
        season.setSeasonNumber(1);
        season.setSeries(series);
        entityManager.persistAndFlush(season);

        Episode episode = new Episode();
        episode.setEpisodeNumber(1);
        episode.setTitle("Episodio 1");
        episode.setSeason(season);
        entityManager.persistAndFlush(episode);

        Subtitle subtitle = new Subtitle();
        subtitle.setLanguage("Español");
        subtitle.setLanguageCode("es");
        subtitle.setSubtitlePath("episodio1_es.vtt");
        subtitle.setEpisode(episode);
        entityManager.persistAndFlush(subtitle);

        Long subtitleId = subtitle.getId();
        Long episodeId = episode.getId();
        assertTrue(subtitleRepository.findById(subtitleId).isPresent());

        entityManager.clear();
        Episode episodeRecargado = episodeRepository.findById(episodeId).orElseThrow();
        episodeRepository.delete(episodeRecargado);
        entityManager.flush();
        entityManager.clear();

        assertTrue(subtitleRepository.findById(subtitleId).isEmpty());
    }
}