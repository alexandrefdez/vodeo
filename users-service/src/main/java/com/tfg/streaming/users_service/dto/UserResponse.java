package com.tfg.streaming.users_service.dto;

import com.tfg.streaming.users_service.model.User;
import lombok.Data;

@Data
public class UserResponse {
    private Long id;
    private String username;
    private String email;
    private User.Role role;
}