package com.tfg.streaming.users_service.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String username;
    private String currentPassword;
    private String newPassword;
}