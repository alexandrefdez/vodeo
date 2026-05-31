package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class ExternalMovieDto {
    private String title;
    private String description;
    private Integer year;
    private String posterUrl;
    private Integer duration;
    private List<String> genres;
}