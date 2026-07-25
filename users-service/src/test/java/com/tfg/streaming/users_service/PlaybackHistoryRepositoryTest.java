package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.model.PlaybackHistory;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.PlaybackHistoryRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PlaybackHistoryRepositoryTest {

    @Autowired
    private PlaybackHistoryRepository playbackHistoryRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByUserAndMediaIdAndMediaType - recupera el progreso guardado para reanudar el video")
    void findProgreso_existente_loEncuentra() {
        User user = new User();
        user.setUsername("alex");
        user.setEmail("alex@vodeo.com");
        user.setPassword("hash");
        entityManager.persistAndFlush(user);

        PlaybackHistory history = new PlaybackHistory();
        history.setUser(user);
        history.setMediaId(5L);
        history.setMediaType("movie");
        history.setCurrentTime(120);
        history.setUpdatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(history);

        Optional<PlaybackHistory> found = playbackHistoryRepository
                .findByUserAndMediaIdAndMediaType(user, 5L, "movie");

        assertTrue(found.isPresent());
        assertEquals(120, found.get().getCurrentTime());
    }

    @Test
    @DisplayName("findByUserAndMediaIdAndMediaType - sin progreso previo devuelve vacio")
    void findProgreso_sinHistorial_devuelveVacio() {
        User user = new User();
        user.setUsername("alex");
        user.setEmail("alex2@vodeo.com");
        user.setPassword("hash");
        entityManager.persistAndFlush(user);

        Optional<PlaybackHistory> found = playbackHistoryRepository
                .findByUserAndMediaIdAndMediaType(user, 99L, "movie");

        assertTrue(found.isEmpty());
    }
}