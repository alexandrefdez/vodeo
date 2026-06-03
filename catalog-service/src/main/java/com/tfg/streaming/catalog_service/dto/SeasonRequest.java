package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class SeasonRequest {
    private Integer seasonNumber;
    private String overview;
}