package com.tfg.streaming.catalog_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "reviews")
@Data
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "media_type", nullable = false)
    private String mediaType;

    @Column(nullable = false)
    private String username;

    @Column(nullable = false)
    private Integer rating; // Del 1 al 5

    @Column(length = 1000)
    private String comment;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}