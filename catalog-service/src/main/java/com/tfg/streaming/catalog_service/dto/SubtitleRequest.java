package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class SubtitleRequest {
    private String language;
    private String languageCode;
    private String subtitlePath;
}