package com.tfg.streaming.catalog_service.dto;

import lombok.Data;
import java.util.List;

@Data
public class SeasonResponse {
    private Long id;
    private Integer seasonNumber;
    private String overview;
    private List<EpisodeResponse> episodes;
}