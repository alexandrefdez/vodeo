package com.tfg.streaming.users_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class WatchlistItemDto {
    private Long mediaId;
    private String mediaType;
}