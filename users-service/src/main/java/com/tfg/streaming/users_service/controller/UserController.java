package com.tfg.streaming.users_service.controller;

import com.tfg.streaming.users_service.dto.AuthResponse;
import com.tfg.streaming.users_service.dto.LoginRequest;
import com.tfg.streaming.users_service.dto.RegisterRequest;
import com.tfg.streaming.users_service.dto.UserResponse;
import com.tfg.streaming.users_service.dto.UpdateProfileRequest;
import com.tfg.streaming.users_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.context.SecurityContextHolder;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/profile")
    public ResponseEntity<UserResponse> getProfile() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(userService.getUserProfile(email));
    }

    @PutMapping("/profile")
    public ResponseEntity<UserResponse> updateProfile(@RequestBody UpdateProfileRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            return ResponseEntity.ok(userService.updateProfile(email, request));
        } catch (RuntimeException e) {
            // Si la contraseña actual es incorrecta, devolvemos un 400 Bad Request
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/count")
    public ResponseEntity<Long> countUsers() {
        return ResponseEntity.ok(userService.countUsers());
    }
}