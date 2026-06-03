package com.tfg.streaming.catalog_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

@Data
public class TmdbMovieDetails {
    private Integer runtime;
    private List<Genre> genres;

    @JsonProperty("backdrop_path")
    private String backdropPath;

    @JsonProperty("vote_average")
    private Double voteAverage;

    // Nodos extraídos mediante append_to_response
    private Credits credits;

    @JsonProperty("release_dates")
    private ReleaseDates releaseDates;

    @Data
    public static class Genre {
        private String name;
    }

    @Data
    public static class Credits {
        private List<Cast> cast;
        private List<Crew> crew;
    }

    @Data
    public static class Cast {
        private String name;
    }

    @Data
    public static class Crew {
        private String job;
        private String name;
    }

    @Data
    public static class ReleaseDates {
        private List<Result> results;
    }

    @Data
    public static class Result {
        @JsonProperty("iso_3166_1")
        private String iso31661;
        @JsonProperty("release_dates")
        private List<ReleaseDate> releaseDates;
    }

    @Data
    public static class ReleaseDate {
        private String certification;
    }
}