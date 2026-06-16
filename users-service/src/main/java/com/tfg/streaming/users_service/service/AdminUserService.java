package com.tfg.streaming.users_service.service;

import com.tfg.streaming.users_service.dto.AdminUserDto;
import com.tfg.streaming.users_service.dto.UserDeletedEvent;
import com.tfg.streaming.users_service.exception.ConflictException;
import com.tfg.streaming.users_service.exception.ResourceNotFoundException;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.PlaybackHistoryRepository;
import com.tfg.streaming.users_service.repository.UserRepository;
import com.tfg.streaming.users_service.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
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
    private final RabbitTemplate rabbitTemplate;

    public List<AdminUserDto> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Proteger a los administradores
        if ("ADMIN".equals(user.getRole().name())) {
            throw new ConflictException("Cannot delete an administrator account");
        }

        // Guardamos el nombre antes de borrarlo para anonimizar las reseñas
        String usernameToAnonymize = user.getUsername();

        // Borramos sus datos privados en cascada
        watchlistRepository.deleteByUser(user);
        historyRepository.deleteByUser(user);

        // Borramos la cuenta de usuario
        userRepository.delete(user);

        // Mandamos el mensaje con RabbitMQ para que anonimice las reseñas de este
        // usuario
        rabbitTemplate.convertAndSend("vodeo.exchange", "user.deleted.key", new UserDeletedEvent(usernameToAnonymize));
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