package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.model.Genre;
import com.tfg.streaming.catalog_service.repository.GenreRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class GenreRepositoryTest {

    @Autowired
    private GenreRepository genreRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByName - encuentra un genero ya existente para el patron Upsert")
    void findByName_generoExistente_loEncuentra() {
        Genre drama = new Genre();
        drama.setName("Drama");
        entityManager.persistAndFlush(drama);

        Optional<Genre> found = genreRepository.findByName("Drama");

        assertTrue(found.isPresent());
        assertEquals("Drama", found.get().getName());
    }

    @Test
    @DisplayName("findByName - genero inexistente devuelve Optional vacio")
    void findByName_generoInexistente_devuelveVacio() {
        Optional<Genre> found = genreRepository.findByName("Comedia");

        assertTrue(found.isEmpty());
    }
}