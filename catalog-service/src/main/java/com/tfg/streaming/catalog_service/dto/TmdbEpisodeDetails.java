package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class TmdbEpisodeDetails {
    private String name;
    private String overview;
    private Integer runtime;
}