package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.OmdbResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class OmdbClient {

    @Value("${omdb.api-key}")
    private String apiKey;

    @Value("${omdb.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public ExternalMovieDto searchMovie(String title) {
        try {
            OmdbResponse response = restClient.get()
                    .uri(baseUrl + "/?apikey=" + apiKey + "&t=" + title)
                    .retrieve()
                    .body(OmdbResponse.class);

            if (response == null || !"True".equals(response.getResponse())) {
                return null;
            }

            ExternalMovieDto dto = new ExternalMovieDto();
            dto.setTitle(response.getTitle());
            dto.setDescription(response.getPlot());
            dto.setYear(parseYear(response.getYear()));

            return dto;

        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseYear(String year) {
        if (year == null || year.length() < 4)
            return null;
        try {
            return Integer.parseInt(year.substring(0, 4));
        } catch (Exception e) {
            return null;
        }
    }
}