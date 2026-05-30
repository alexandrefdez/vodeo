package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class MovieResponse {
    private Long id;
    private String title;
    private String description;
    private Integer year;
    private Integer duration;
    private String videoPath;
    private String posterUrl;
    private List<String> genres;
}