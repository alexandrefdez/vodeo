package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class TmdbSearchResponse {
    private List<TmdbMovieResult> results;
}