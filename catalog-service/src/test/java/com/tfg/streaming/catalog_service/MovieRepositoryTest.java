package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.model.Genre;
import com.tfg.streaming.catalog_service.model.Movie;
import com.tfg.streaming.catalog_service.repository.MovieRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = "spring.jpa.properties.hibernate.globally_quoted_identifiers=true")
class MovieRepositoryTest {

    @Autowired
    private MovieRepository movieRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findById - devuelve la pelicula con sus generos ya cargados")
    void findById_devuelvePeliculaConGeneros() {
        Genre drama = new Genre();
        drama.setName("Drama");
        drama = entityManager.persistAndFlush(drama);

        Movie movie = new Movie();
        movie.setTitle("Inception");
        movie.setDescription("...");
        movie.setYear(2010);
        movie.setDuration(148);
        movie.setVideoPath("inception.mp4");
        movie.setGenres(new ArrayList<>(List.of(drama)));

        Movie saved = movieRepository.save(movie);
        entityManager.flush();
        entityManager.clear();

        Optional<Movie> found = movieRepository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(1, found.get().getGenres().size());
        assertEquals("Drama", found.get().getGenres().get(0).getName());
    }
}