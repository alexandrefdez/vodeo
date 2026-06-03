package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class EpisodeResponse {
    private Long id;
    private Integer episodeNumber;
    private String title;
    private String description;
    private Integer duration;
    private String videoPath;
    private Long seasonId;
}