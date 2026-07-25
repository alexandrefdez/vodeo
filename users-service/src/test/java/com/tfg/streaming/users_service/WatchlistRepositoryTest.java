package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.model.Watchlist;
import com.tfg.streaming.users_service.repository.WatchlistRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class WatchlistRepositoryTest {

    @Autowired
    private WatchlistRepository watchlistRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByUserAndMediaIdAndMediaType - detecta si una pelicula ya esta en Mi Lista")
    void findByUserAndMediaIdAndMediaType_yaGuardado_loEncuentra() {
        User user = new User();
        user.setUsername("alex");
        user.setEmail("alex@vodeo.com");
        user.setPassword("hash");
        entityManager.persistAndFlush(user);

        Watchlist item = new Watchlist();
        item.setUser(user);
        item.setMediaId(1L);
        item.setMediaType("movie");
        entityManager.persistAndFlush(item);

        Optional<Watchlist> found = watchlistRepository
                .findByUserAndMediaIdAndMediaType(user, 1L, "movie");

        assertTrue(found.isPresent());
    }

    @Test
    @DisplayName("findByUserAndMediaIdAndMediaType - pelicula no guardada devuelve vacio")
    void findByUserAndMediaIdAndMediaType_noGuardado_devuelveVacio() {
        User user = new User();
        user.setUsername("alex");
        user.setEmail("alex2@vodeo.com");
        user.setPassword("hash");
        entityManager.persistAndFlush(user);

        Optional<Watchlist> found = watchlistRepository
                .findByUserAndMediaIdAndMediaType(user, 99L, "movie");

        assertTrue(found.isEmpty());
    }
}