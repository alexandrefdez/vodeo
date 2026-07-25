package com.tfg.streaming.catalog_service.controller;

import com.tfg.streaming.catalog_service.config.JwtService;
import com.tfg.streaming.catalog_service.dto.ReviewDto;
import com.tfg.streaming.catalog_service.service.ReviewService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@RestController
@RequestMapping("/api/catalog/reviews")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;
    private final JwtService jwtService;

    @GetMapping("/{mediaType}/{mediaId}")
    public ResponseEntity<Page<ReviewDto>> getReviews(
            @PathVariable String mediaType,
            @PathVariable Long mediaId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        return ResponseEntity.ok(reviewService.getReviews(mediaType, mediaId, page, size));
    }

    @PostMapping
    public ResponseEntity<ReviewDto> addReview(@RequestBody ReviewDto request) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String authHeader = attributes.getRequest().getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        String username = jwtService.extractUsername(token);

        return ResponseEntity.status(HttpStatus.CREATED).body(reviewService.addReview(request, username));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.noContent().build();
    }
}