package com.tfg.streaming.catalog_service.controller;

import com.tfg.streaming.catalog_service.dto.ReviewDto;
import com.tfg.streaming.catalog_service.model.Review;
import com.tfg.streaming.catalog_service.repository.ReviewRepository;
import com.tfg.streaming.catalog_service.config.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@RestController
@RequestMapping("/api/catalog/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewRepository reviewRepository;
    private final JwtService jwtService;

    // Endpoint PÚBLICO para leer las reseñas
    @GetMapping("/{mediaType}/{mediaId}")
    public ResponseEntity<Page<ReviewDto>> getReviews(
            @PathVariable String mediaType,
            @PathVariable Long mediaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {

        Page<Review> reviews = reviewRepository.findByMediaIdAndMediaTypeOrderByCreatedAtDesc(
                mediaId, mediaType, PageRequest.of(page, size));

        // Mapear entidad a DTO
        Page<ReviewDto> dtos = reviews.map(this::toDto);
        return ResponseEntity.ok(dtos);
    }

    // Endpoint PRIVADO para publicar una reseña
    @PostMapping
    public ResponseEntity<?> addReview(@RequestBody ReviewDto request) {
        try {
            // Extraer el token de la peticion HTTP actual
            ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                    .getRequestAttributes();
            if (attributes == null)
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            String authHeader = attributes.getRequest().getHeader("Authorization");
            if (authHeader == null || !authHeader.startsWith("Bearer "))
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

            // Extraer el Username firmado del Token (Sin consultar al users-service ya lo
            // miramos directamente en el token)
            String token = authHeader.substring(7);
            String username = jwtService.extractUsername(token);

            // 3. Validar si ya opinó
            Optional<Review> existing = reviewRepository.findByUsernameAndMediaIdAndMediaType(
                    username, request.getMediaId(), request.getMediaType());

            if (existing.isPresent())
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Ya has valorado este título.");

            // 4. Guardar reseña
            // Construir entidad desde DTO (nunca desde la entidad directa)
            Review review = new Review();
            review.setMediaId(request.getMediaId());
            review.setMediaType(request.getMediaType());
            review.setRating(request.getRating());
            review.setComment(request.getComment());
            review.setUsername(username);
            review.setCreatedAt(java.time.LocalDateTime.now());

            Review saved = reviewRepository.save(review);
            return ResponseEntity.status(HttpStatus.CREATED).body(toDto(saved));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error al guardar reseña.");
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        try {
            reviewRepository.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    // Mapper privado entidad a DTO
    private ReviewDto toDto(Review review) {
        ReviewDto dto = new ReviewDto();
        dto.setId(review.getId());
        dto.setMediaId(review.getMediaId());
        dto.setMediaType(review.getMediaType());
        dto.setUsername(review.getUsername());
        dto.setRating(review.getRating());
        dto.setComment(review.getComment());
        dto.setCreatedAt(review.getCreatedAt());
        return dto;
    }
}