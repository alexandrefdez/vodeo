package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.dto.WatchlistItemDto;
import com.tfg.streaming.users_service.exception.ResourceNotFoundException;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.model.Watchlist;
import com.tfg.streaming.users_service.repository.UserRepository;
import com.tfg.streaming.users_service.repository.WatchlistRepository;
import com.tfg.streaming.users_service.service.WatchlistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("WatchlistService — Tests Unitarios")
class WatchlistServiceTest {

    @Mock
    private WatchlistRepository watchlistRepository;
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private WatchlistService watchlistService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail("test@vodeo.com");
    }

    @Test
    @DisplayName("toggleWatchlist — item no existe → lo añade")
    void testToggle_AddItem() {
        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        when(watchlistRepository.findByUserAndMediaIdAndMediaType(mockUser, 10L, "movie"))
                .thenReturn(Optional.empty());

        watchlistService.toggleWatchlist("test@vodeo.com", 10L, "movie");

        verify(watchlistRepository, times(1)).save(any(Watchlist.class));
        verify(watchlistRepository, never()).delete(any());
    }

    @Test
    @DisplayName("toggleWatchlist — item existe → lo elimina")
    void testToggle_RemoveItem() {
        Watchlist existing = new Watchlist();
        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        when(watchlistRepository.findByUserAndMediaIdAndMediaType(mockUser, 10L, "movie"))
                .thenReturn(Optional.of(existing));

        watchlistService.toggleWatchlist("test@vodeo.com", 10L, "movie");

        verify(watchlistRepository, times(1)).delete(existing);
        verify(watchlistRepository, never()).save(any());
    }

    @Test
    @DisplayName("getUserWatchlist — devuelve página mapeada")
    void testGetUserWatchlist_ReturnsMappedPage() {
        Watchlist w = new Watchlist();
        w.setMediaId(5L);
        w.setMediaType("series");
        w.setUser(mockUser);

        Page<Watchlist> page = new PageImpl<>(List.of(w));

        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        when(watchlistRepository.findByUserOrderByIdDesc(eq(mockUser), any(PageRequest.class)))
                .thenReturn(page);

        Page<WatchlistItemDto> result = watchlistService.getUserWatchlist("test@vodeo.com", 0, 12);

        assertEquals(1, result.getTotalElements());
        assertEquals(5L, result.getContent().get(0).getMediaId());
    }
}