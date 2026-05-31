package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.TmdbMovieDetails;
import com.tfg.streaming.catalog_service.dto.TmdbMovieResult;
import com.tfg.streaming.catalog_service.dto.TmdbSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class TmdbClient {

    @Value("${tmdb.api-key}")
    private String apiKey;

    @Value("${tmdb.base-url}")
    private String baseUrl;

    @Value("${tmdb.image-base-url}")
    private String imageBaseUrl;

    private final RestClient restClient = RestClient.create();

    public ExternalMovieDto searchMovie(String title) {
        try {
            TmdbSearchResponse response = restClient.get()
                    .uri(baseUrl + "/search/movie?api_key=" + apiKey + "&query=" + title + "&language=es-ES")
                    .retrieve()
                    .body(TmdbSearchResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                return null;
            }

            TmdbMovieResult result = response.getResults().get(0);
            Integer runtime = null;
            List<String> genreNames = null;

            try {
                TmdbMovieDetails details = restClient.get()
                        .uri(baseUrl + "/movie/" + result.getId() + "?api_key=" + apiKey + "&language=es-ES")
                        .retrieve()
                        .body(TmdbMovieDetails.class);

                if (details != null) {
                    runtime = details.getRuntime();
                    if (details.getGenres() != null) {
                        genreNames = details.getGenres().stream()
                                .map(TmdbMovieDetails.Genre::getName)
                                .toList();
                    }
                }
            } catch (Exception e) {
                System.err.println("Error fetching details: " + e.getMessage());
            }

            ExternalMovieDto dto = new ExternalMovieDto();
            dto.setTitle(result.getTitle());
            dto.setDescription(result.getOverview());
            dto.setYear(parseYear(result.getReleaseDate()));
            dto.setPosterUrl(result.getPosterPath() != null ? imageBaseUrl + result.getPosterPath() : null);
            dto.setDuration(runtime);
            dto.setGenres(genreNames);

            return dto;

        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseYear(String releaseDate) {
        if (releaseDate == null || releaseDate.length() < 4)
            return null;
        try {
            return Integer.parseInt(releaseDate.substring(0, 4));
        } catch (Exception e) {
            return null;
        }
    }
}