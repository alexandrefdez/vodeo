package com.tfg.streaming.catalog_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MediaDeletedEvent {
    private String mediaType;
    private Long mediaId;
}