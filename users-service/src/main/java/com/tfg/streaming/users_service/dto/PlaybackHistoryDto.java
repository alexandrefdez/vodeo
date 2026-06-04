package com.tfg.streaming.users_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class PlaybackHistoryDto {
    private Long mediaId;
    private String mediaType;
    private Integer currentTime;
    private Integer duration;
    private LocalDateTime updatedAt;
}