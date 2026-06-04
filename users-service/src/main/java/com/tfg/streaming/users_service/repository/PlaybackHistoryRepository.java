package com.tfg.streaming.users_service.repository;

import com.tfg.streaming.users_service.model.PlaybackHistory;
import com.tfg.streaming.users_service.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlaybackHistoryRepository extends JpaRepository<PlaybackHistory, Long> {

    // Buscar un progreso específico para reanudar el vídeo
    Optional<PlaybackHistory> findByUserAndMediaIdAndMediaType(User user, Long mediaId, String mediaType);

    // Obtener los últimos vídeos vistos por el usuario
    Page<PlaybackHistory> findByUserOrderByUpdatedAtDesc(User user, Pageable pageable);

    void deleteByUser(User user);
}