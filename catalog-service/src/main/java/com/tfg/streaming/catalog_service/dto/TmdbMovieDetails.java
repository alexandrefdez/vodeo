package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class TmdbMovieDetails {
    private Integer runtime;
    private List<Genre> genres;

    @Data
    public static class Genre {
        private String name;
    }
}