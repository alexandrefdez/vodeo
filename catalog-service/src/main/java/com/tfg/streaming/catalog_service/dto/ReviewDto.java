package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ReviewDto {
    private Long id;
    private Long mediaId;
    private String mediaType;
    private String username;
    private Integer rating;
    private String comment;
    private LocalDateTime createdAt;
}