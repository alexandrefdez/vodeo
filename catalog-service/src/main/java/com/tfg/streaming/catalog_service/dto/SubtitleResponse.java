package com.tfg.streaming.catalog_service.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SubtitleResponse implements Serializable {
    private Long id;
    private String language;
    private String languageCode;
    private String subtitlePath;
}