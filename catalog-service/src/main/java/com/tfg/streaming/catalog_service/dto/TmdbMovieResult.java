package com.tfg.streaming.catalog_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TmdbMovieResult {
    private String title;
    private String overview;

    @JsonProperty("release_date")
    private String releaseDate;
}