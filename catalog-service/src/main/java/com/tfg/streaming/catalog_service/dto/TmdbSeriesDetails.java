package com.tfg.streaming.catalog_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbSeriesDetails {
    private List<TmdbMovieDetails.Genre> genres;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("vote_average")
    private Double voteAverage;

    private TmdbMovieDetails.Credits credits;

    @JsonProperty("content_ratings")
    private ContentRatings contentRatings;

    @JsonProperty("created_by")
    private List<Creator> createdBy;

    @Data
    public static class ContentRatings {
        private List<RatingResult> results;
    }

    @Data
    public static class RatingResult {
        @JsonProperty("iso_3166_1")
        private String iso31661;
        private String rating;
    }

    @Data
    public static class Creator {
        private String name;
    }
}