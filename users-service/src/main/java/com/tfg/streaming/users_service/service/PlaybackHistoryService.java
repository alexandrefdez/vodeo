package com.tfg.streaming.users_service.service;

import com.tfg.streaming.users_service.dto.PlaybackHistoryDto;
import com.tfg.streaming.users_service.model.PlaybackHistory;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.PlaybackHistoryRepository;
import com.tfg.streaming.users_service.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PlaybackHistoryService {

    private final PlaybackHistoryRepository historyRepository;
    private final UserRepository userRepository;

    public void saveProgress(String email, PlaybackHistoryDto dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Miramos si ya hay un progreso guardado para este vídeo, si no lo hay creamos
        // uno nuevo
        Optional<PlaybackHistory> existing = historyRepository.findByUserAndMediaIdAndMediaType(
                user, dto.getMediaId(), dto.getMediaType());

        PlaybackHistory history = existing.orElse(new PlaybackHistory());

        history.setUser(user);
        history.setMediaId(dto.getMediaId());
        history.setMediaType(dto.getMediaType());
        history.setCurrentTime(dto.getCurrentTime());
        history.setDuration(dto.getDuration());
        history.setUpdatedAt(LocalDateTime.now());

        historyRepository.save(history);
    }

    public Page<PlaybackHistoryDto> getUserHistory(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return historyRepository.findByUserOrderByUpdatedAtDesc(user, PageRequest.of(page, size))
                .map(h -> {
                    PlaybackHistoryDto dto = new PlaybackHistoryDto();
                    dto.setMediaId(h.getMediaId());
                    dto.setMediaType(h.getMediaType());
                    dto.setCurrentTime(h.getCurrentTime());
                    dto.setDuration(h.getDuration());
                    dto.setUpdatedAt(h.getUpdatedAt());
                    return dto;
                });
    }

    public Integer getProgress(String email, Long mediaId, String mediaType) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return historyRepository.findByUserAndMediaIdAndMediaType(user, mediaId, mediaType)
                .map(PlaybackHistory::getCurrentTime)
                .orElse(0); // Si nunca lo ha visto devuelve el segundo 0
    }
}