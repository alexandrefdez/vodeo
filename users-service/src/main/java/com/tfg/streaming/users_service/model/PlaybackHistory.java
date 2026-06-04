package com.tfg.streaming.users_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "playback_history", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "user_id", "media_id", "media_type" })
})
@Data
public class PlaybackHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "media_id", nullable = false)
    private Long mediaId;

    @Column(name = "media_type", nullable = false)
    private String mediaType; // "movie" o "episode"

    @Column(name = "current_time_sec", nullable = false)
    private Integer currentTime; // Segundo exacto donde lo dejó

    @Column(name = "duration_sec")
    private Integer duration; // Duración total para calcular la barra de progreso

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt; // Para saber qué fue lo último que vio para el historial
}