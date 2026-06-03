package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class TmdbSeriesSearchResponse {
    private List<TmdbSeriesResult> results;
}