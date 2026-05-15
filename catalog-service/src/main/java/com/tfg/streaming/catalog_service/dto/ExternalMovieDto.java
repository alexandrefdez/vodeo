package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class ExternalMovieDto {
    private String title;
    private String description;
    private Integer year;
}