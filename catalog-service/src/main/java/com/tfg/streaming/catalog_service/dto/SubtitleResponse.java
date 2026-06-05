package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class SubtitleResponse {
    private Long id;
    private String language;
    private String languageCode;
    private String subtitlePath;
}