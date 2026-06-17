package com.tfg.streaming.catalog_service.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class EpisodeResponse implements Serializable {
    private Long id;
    private Integer episodeNumber;
    private String title;
    private String description;
    private Integer duration;
    private String videoPath;
    private Long seasonId;
    private String seriesTitle;
    private String posterUrl;

    private Long seriesId;
    private List<String> seriesGenres;

    private List<SubtitleResponse> subtitles = new ArrayList<>();
}