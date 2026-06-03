package com.tfg.streaming.users_service.model;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "watchlist", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "media_id", "media_type" })
})
@Data
public class Watchlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "media_type", nullable = false)
    private String mediaType; // Guarda "movie" o "series"
}