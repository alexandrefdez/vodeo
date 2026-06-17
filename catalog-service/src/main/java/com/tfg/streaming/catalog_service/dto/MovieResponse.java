package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Data
public class MovieResponse implements Serializable {
    private Long id;
    private String title;
    private String description;
    private Integer year;
    private Integer duration;
    private String videoPath;
    private String posterUrl;
    private List<String> genres;

    // Campos para pagina de detalles
    private String backdropUrl;
    private Double tmdbRating;
    private String contentRating;
    private String director;
    private String castCrew;

    private List<SubtitleResponse> subtitles = new ArrayList<>();
}