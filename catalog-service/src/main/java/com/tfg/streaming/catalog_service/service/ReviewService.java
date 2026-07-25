package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ReviewDto;
import com.tfg.streaming.catalog_service.exception.ConflictException;
import com.tfg.streaming.catalog_service.model.Review;
import com.tfg.streaming.catalog_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public Page<ReviewDto> getReviews(String mediaType, Long mediaId, int page, int size) {
        Page<Review> reviews = reviewRepository.findByMediaIdAndMediaTypeOrderByCreatedAtDesc(
                mediaId, mediaType, PageRequest.of(page, size));
        return reviews.map(this::toDto);
    }

    public ReviewDto addReview(ReviewDto request, String username) {
        Optional<Review> existing = reviewRepository.findByUsernameAndMediaIdAndMediaType(
                username, request.getMediaId(), request.getMediaType());

        if (existing.isPresent()) {
            throw new ConflictException("Ya has valorado este título.");
        }

        Review review = new Review();
        review.setMediaId(request.getMediaId());
        review.setMediaType(request.getMediaType());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        review.setUsername(username);
        review.setCreatedAt(LocalDateTime.now());

        return toDto(reviewRepository.save(review));
    }

    public void deleteReview(Long id) {
        reviewRepository.deleteById(id);
    }

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