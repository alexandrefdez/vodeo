package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class SeriesRequest {
    private String title;
    private String description;
    private Integer startYear;
    private String posterUrl;
    private String backdropUrl;
    private Double tmdbRating;
    private String contentRating;
    private String director;
    private String castCrew;
    private List<String> genres;
}