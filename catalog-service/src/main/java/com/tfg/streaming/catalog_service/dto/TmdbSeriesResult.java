package com.tfg.streaming.catalog_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class TmdbSeriesResult {
    private Long id;
    private String name;
    private String overview;

    @JsonProperty("first_air_date")
    private String firstAirDate;

    @JsonProperty("poster_path")
    private String posterPath;
}