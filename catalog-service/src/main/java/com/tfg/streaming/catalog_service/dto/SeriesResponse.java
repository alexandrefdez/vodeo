package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SeriesResponse implements Serializable {
    private Long id;
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