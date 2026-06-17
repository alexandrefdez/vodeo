package com.tfg.streaming.catalog_service.dto;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SeasonResponse implements Serializable {
    private Long id;
    private Integer seasonNumber;
    private String overview;
    private List<EpisodeResponse> episodes;
}