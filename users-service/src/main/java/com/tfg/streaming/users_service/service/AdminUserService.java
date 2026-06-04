package com.tfg.streaming.users_service.service;

import com.tfg.streaming.users_service.dto.AdminUserDto;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.PlaybackHistoryRepository;
import com.tfg.streaming.users_service.repository.UserRepository;
import com.tfg.streaming.users_service.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserRepository userRepository;
    private final PlaybackHistoryRepository historyRepository;
    private final WatchlistRepository watchlistRepository;

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        // Proteger a los administradores
        if ("ADMIN".equals(user.getRole().name())) {
            throw new RuntimeException("No se puede eliminar a un administrador desde el panel");
        }

        // Borramos sus datos privados en cascada
        watchlistRepository.deleteByUser(user);
        historyRepository.deleteByUser(user);

        // Borramos la cuenta de usuario
        userRepository.delete(user);
    }

    private AdminUserDto mapToDto(User user) {
        AdminUserDto dto = new AdminUserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setRole(user.getRole().name());
        return dto;
    }
}