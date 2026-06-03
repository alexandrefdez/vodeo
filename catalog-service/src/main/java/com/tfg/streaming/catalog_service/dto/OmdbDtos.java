package com.tfg.streaming.catalog_service.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.util.List;

public class OmdbDtos {

    // Clase para mapear la primera petición (Búsqueda múltiple con s=)
    @Data
    public static class SearchResponse {
        @JsonProperty("Search")
        private List<SearchResult> search;

        @JsonProperty("Response")
        private String response;
    }

    // El resultado básico de la lista
    @Data
    public static class SearchResult {
        @JsonProperty("Title")
        private String title;

        @JsonProperty("imdbID")
        private String imdbId;
    }

    // Clase para mapear la segunda petición (Detalles completos con i=)
    @Data
    public static class DetailsResponse {
        @JsonProperty("Title")
        private String title;

        @JsonProperty("Year")
        private String year;

        @JsonProperty("Rated")
        private String rated;

        @JsonProperty("Runtime")
        private String runtime;

        @JsonProperty("Genre")
        private String genre;

        @JsonProperty("Director")
        private String director;

        @JsonProperty("Actors")
        private String actors;

        @JsonProperty("Plot")
        private String plot;

        @JsonProperty("Poster")
        private String poster;

        @JsonProperty("imdbRating")
        private String imdbRating;

        @JsonProperty("Response")
        private String response;
    }
}