package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class ExternalEpisodeDto {
    private String title;
    private String description;
    private Integer duration;
}