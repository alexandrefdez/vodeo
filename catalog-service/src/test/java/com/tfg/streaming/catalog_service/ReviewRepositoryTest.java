package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.model.Review;
import com.tfg.streaming.catalog_service.repository.ReviewRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private TestEntityManager entityManager;

    @Test
    @DisplayName("findByUsernameAndMediaIdAndMediaType - detecta si el usuario ya opino sobre ese contenido")
    void yaOpino_detectaResenaExistente() {
        Review review = new Review();
        review.setMediaId(10L);
        review.setMediaType("movie");
        review.setUsername("alex");
        review.setRating(5);
        review.setComment("Genial");
        review.setCreatedAt(LocalDateTime.now());
        entityManager.persistAndFlush(review);

        Optional<Review> found = reviewRepository.findByUsernameAndMediaIdAndMediaType("alex", 10L, "movie");

        assertTrue(found.isPresent());
        assertEquals(5, found.get().getRating());
    }

    @Test
    @DisplayName("findByUsernameAndMediaIdAndMediaType - usuario sin resena previa devuelve vacio")
    void sinResenaPrevia_devuelveVacio() {
        Optional<Review> found = reviewRepository.findByUsernameAndMediaIdAndMediaType("otro", 10L, "movie");

        assertTrue(found.isEmpty());
    }
}