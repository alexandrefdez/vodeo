package com.tfg.streaming.catalog_service.repository;

import com.tfg.streaming.catalog_service.model.Review;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // Traer todas las reseñas de una peli/serie paginadas y ordenadas por las más
    // recientes
    Page<Review> findByMediaIdAndMediaTypeOrderByCreatedAtDesc(Long mediaId, String mediaType, Pageable pageable);

    // Comprobar si el usuario ya ha opinado para evitar spam
    Optional<Review> findByUsernameAndMediaIdAndMediaType(String username, Long mediaId, String mediaType);
}