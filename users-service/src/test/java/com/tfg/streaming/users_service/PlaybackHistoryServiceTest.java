package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.dto.PlaybackHistoryDto;
import com.tfg.streaming.users_service.exception.ResourceNotFoundException;
import com.tfg.streaming.users_service.model.PlaybackHistory;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.PlaybackHistoryRepository;
import com.tfg.streaming.users_service.repository.UserRepository;
import com.tfg.streaming.users_service.service.PlaybackHistoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PlaybackHistoryService — Tests Unitarios")
class PlaybackHistoryServiceTest {

    @Mock
    private PlaybackHistoryRepository historyRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PlaybackHistoryService playbackHistoryService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setEmail("test@vodeo.com");
    }

    @Test
    @DisplayName("saveProgress — nuevo registro de visualización")
    void testSaveProgress_NewRecord() {
        PlaybackHistoryDto dto = new PlaybackHistoryDto();
        dto.setMediaId(42L);
        dto.setMediaType("movie");
        dto.setCurrentTime(300);

        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        when(historyRepository.findByUserAndMediaIdAndMediaType(mockUser, 42L, "movie"))
                .thenReturn(Optional.empty());

        playbackHistoryService.saveProgress("test@vodeo.com", dto);
        verify(historyRepository, times(1)).save(any(PlaybackHistory.class));
    }

    @Test
    @DisplayName("saveProgress — actualiza registro existente (Upsert)")
    void testSaveProgress_UpdatesExistingRecord() {
        PlaybackHistory existing = new PlaybackHistory();
        existing.setCurrentTime(60);
        existing.setDuration(7200);

        PlaybackHistoryDto dto = new PlaybackHistoryDto();
        dto.setMediaId(42L);
        dto.setMediaType("movie");
        dto.setCurrentTime(300);

        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        when(historyRepository.findByUserAndMediaIdAndMediaType(mockUser, 42L, "movie"))
                .thenReturn(Optional.of(existing));

        playbackHistoryService.saveProgress("test@vodeo.com", dto);

        ArgumentCaptor<PlaybackHistory> captor = ArgumentCaptor.forClass(PlaybackHistory.class);
        verify(historyRepository, times(1)).save(captor.capture());
        assertEquals(300, captor.getValue().getCurrentTime());
    }

    @Test
    @DisplayName("saveProgress — lanza excepción si el usuario no existe")
    void testSaveProgress_UserNotFound() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> playbackHistoryService.saveProgress("fake@email.com", new PlaybackHistoryDto()));
    }
}