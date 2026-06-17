package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.dto.UserDeletedEvent;
import com.tfg.streaming.users_service.exception.ConflictException;
import com.tfg.streaming.users_service.exception.ResourceNotFoundException;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.PlaybackHistoryRepository;
import com.tfg.streaming.users_service.repository.UserRepository;
import com.tfg.streaming.users_service.repository.WatchlistRepository;
import com.tfg.streaming.users_service.service.AdminUserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminUserService — Tests Unitarios")
class AdminUserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PlaybackHistoryRepository historyRepository;
    @Mock
    private WatchlistRepository watchlistRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AdminUserService adminUserService;

    private User normalUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setRole(User.Role.USER);

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setRole(User.Role.ADMIN);
    }

    @Test
    @DisplayName("deleteUser — borrado en cascada para usuario normal")
    void testDeleteUser_NormalUser_Success() {
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        adminUserService.deleteUser(2L);

        verify(watchlistRepository, times(1)).deleteByUser(normalUser);
        verify(historyRepository, times(1)).deleteByUser(normalUser);
        verify(userRepository, times(1)).delete(normalUser);
        verify(rabbitTemplate, times(1)).convertAndSend(
                eq("vodeo.exchange"), eq("user.deleted.key"), any(UserDeletedEvent.class));
    }

    @Test
    @DisplayName("deleteUser — protección contra borrado de ADMIN")
    void testDeleteUser_AdminUser_ThrowsConflictException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        assertThrows(ConflictException.class, () -> adminUserService.deleteUser(1L));
        verify(userRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteUser — usuario inexistente")
    void testDeleteUser_NotFound() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> adminUserService.deleteUser(999L));
    }
}