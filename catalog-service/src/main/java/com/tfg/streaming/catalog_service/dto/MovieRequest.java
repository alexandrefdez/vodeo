package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class MovieRequest {
    private String title;
    private String description;
    private Integer year;
    private Integer duration;
    private List<Long> genreIds;
}