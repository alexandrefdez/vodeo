package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.TmdbMovieResult;
import com.tfg.streaming.catalog_service.dto.TmdbSearchResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class TmdbClient {

    @Value("${tmdb.api-key}")
    private String apiKey;

    @Value("${tmdb.base-url}")
    private String baseUrl;

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

            ExternalMovieDto dto = new ExternalMovieDto();
            dto.setTitle(result.getTitle());
            dto.setDescription(result.getOverview());
            dto.setYear(parseYear(result.getReleaseDate()));

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