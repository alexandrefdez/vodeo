package com.tfg.streaming.users_service;

import com.tfg.streaming.users_service.config.JwtService;
import com.tfg.streaming.users_service.dto.*;
import com.tfg.streaming.users_service.exception.ConflictException;
import com.tfg.streaming.users_service.exception.ResourceNotFoundException;
import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.repository.UserRepository;
import com.tfg.streaming.users_service.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService — Tests Unitarios")
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private BCryptPasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;

    @InjectMocks
    private UserService userService;

    private User mockUser;

    @BeforeEach
    void setUp() {
        mockUser = new User();
        mockUser.setId(1L);
        mockUser.setUsername("testuser");
        mockUser.setEmail("test@vodeo.com");
        mockUser.setPassword("encodedPassword");
        mockUser.setRole(User.Role.USER);
    }

    @Test
    @DisplayName("register — email nuevo → persiste y devuelve UserResponse")
    void testRegister_Success() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("testuser");
        request.setEmail("test@vodeo.com");
        request.setPassword("plainPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.getPassword())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals("testuser", response.getUsername());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("register — email duplicado → lanza ConflictException")
    void testRegister_EmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest();
        request.setEmail("test@vodeo.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));

        assertThrows(ConflictException.class, () -> userService.register(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("login — credenciales correctas → devuelve AuthResponse con JWT")
    void testLogin_Success() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@vodeo.com");
        request.setPassword("plainPassword");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getPassword(), mockUser.getPassword())).thenReturn(true);
        when(jwtService.generateToken(mockUser.getEmail(), mockUser.getRole().name(), mockUser.getUsername()))
                .thenReturn("mocked.jwt.token");

        AuthResponse response = userService.login(request);

        assertNotNull(response);
        assertEquals("mocked.jwt.token", response.getToken());
    }

    @Test
    @DisplayName("login — email no registrado → lanza ResourceNotFoundException")
    void testLogin_UserNotFound() {
        LoginRequest request = new LoginRequest();
        request.setEmail("noexiste@vodeo.com");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("login — contraseña incorrecta → lanza ConflictException")
    void testLogin_WrongPassword() {
        LoginRequest request = new LoginRequest();
        request.setEmail("test@vodeo.com");
        request.setPassword("wrong");

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(request.getPassword(), mockUser.getPassword())).thenReturn(false);

        assertThrows(ConflictException.class, () -> userService.login(request));
    }

    @Test
    @DisplayName("getUserProfile — usuario existente → devuelve perfil")
    void testGetUserProfile_Success() {
        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        UserResponse response = userService.getUserProfile("test@vodeo.com");
        assertEquals("testuser", response.getUsername());
    }

    @Test
    @DisplayName("updateProfile — actualiza username correctamente")
    void testUpdateProfile_UsernameChange() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("nuevoNombre");

        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        when(userRepository.save(any(User.class))).thenReturn(mockUser);

        userService.updateProfile("test@vodeo.com", request);
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("updateProfile — password actual incorrecta → lanza ConflictException")
    void testUpdateProfile_WrongCurrentPassword() {
        UpdateProfileRequest request = new UpdateProfileRequest();
        request.setUsername("testuser");
        request.setCurrentPassword("incorrecta");
        request.setNewPassword("nueva");

        when(userRepository.findByEmail("test@vodeo.com")).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches("incorrecta", "encodedPassword")).thenReturn(false);

        assertThrows(ConflictException.class, () -> userService.updateProfile("test@vodeo.com", request));
    }
}