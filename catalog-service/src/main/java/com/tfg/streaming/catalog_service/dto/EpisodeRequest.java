package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

@Data
public class EpisodeRequest {
    private Integer episodeNumber;
    private String title;
    private String description;
    private Integer duration;
    private String videoPath;
}