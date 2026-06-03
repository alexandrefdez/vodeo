package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class MovieRequest {
    private String title;
    private String description;
    private Integer year;
    private Integer duration;
    private String posterUrl;
    private String videoPath;
    private List<String> genres;

    // Nuevos campos para pagina de detalles
    private String backdropUrl;
    private Double tmdbRating;
    private String contentRating;
    private String director;
    private String castCrew;
}