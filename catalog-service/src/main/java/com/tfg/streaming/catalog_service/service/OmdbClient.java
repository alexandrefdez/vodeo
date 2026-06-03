package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.ExternalSeriesDto;
import com.tfg.streaming.catalog_service.dto.OmdbDtos;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class OmdbClient {

    @Value("${omdb.api-key}")
    private String apiKey;
    @Value("${omdb.base-url}")
    private String baseUrl;

    private final RestClient restClient = RestClient.create();

    public List<ExternalMovieDto> searchMovies(String title) {
        try {
            OmdbDtos.SearchResponse searchRes = restClient.get()
                    .uri(baseUrl + "/?apikey=" + apiKey + "&s=" + title)
                    .retrieve()
                    .body(OmdbDtos.SearchResponse.class);

            if (searchRes == null || !"True".equals(searchRes.getResponse()) || searchRes.getSearch() == null) {
                return List.of();
            }

            List<ExternalMovieDto> dtos = new ArrayList<>();
            List<OmdbDtos.SearchResult> topResults = searchRes.getSearch().stream().limit(5).toList();

            for (OmdbDtos.SearchResult result : topResults) {
                try {
                    OmdbDtos.DetailsResponse details = restClient.get()
                            .uri(baseUrl + "/?apikey=" + apiKey + "&i=" + result.getImdbId())
                            .retrieve()
                            .body(OmdbDtos.DetailsResponse.class);

                    if (details != null && "True".equals(details.getResponse())) {
                        ExternalMovieDto dto = new ExternalMovieDto();
                        dto.setTitle(details.getTitle());
                        dto.setDescription(!"N/A".equals(details.getPlot()) ? details.getPlot() : null);
                        dto.setYear(parseYear(details.getYear()));
                        dto.setPosterUrl(!"N/A".equals(details.getPoster()) ? details.getPoster() : null);

                        dto.setDirector(!"N/A".equals(details.getDirector()) ? details.getDirector() : null);
                        dto.setCastCrew(!"N/A".equals(details.getActors()) ? details.getActors() : null);
                        dto.setContentRating(!"N/A".equals(details.getRated()) ? details.getRated() : null);

                        dto.setDuration(parseRuntime(details.getRuntime()));
                        dto.setTmdbRating(parseRating(details.getImdbRating()));

                        if (details.getGenre() != null && !"N/A".equals(details.getGenre())) {
                            dto.setGenres(Arrays.stream(details.getGenre().split(",")).map(String::trim).toList());
                        }
                        dtos.add(dto);
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando OMDb ID: " + result.getImdbId());
                }
            }
            return dtos;
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<ExternalSeriesDto> searchSeries(String title) {
        try {
            OmdbDtos.SearchResponse searchRes = restClient.get()
                    .uri(baseUrl + "/?apikey=" + apiKey + "&s=" + title + "&type=series")
                    .retrieve()
                    .body(OmdbDtos.SearchResponse.class);

            if (searchRes == null || !"True".equals(searchRes.getResponse()) || searchRes.getSearch() == null) {
                return List.of();
            }

            List<ExternalSeriesDto> dtos = new ArrayList<>();
            List<OmdbDtos.SearchResult> topResults = searchRes.getSearch().stream().limit(5).toList();

            for (OmdbDtos.SearchResult result : topResults) {
                try {
                    OmdbDtos.DetailsResponse details = restClient.get()
                            .uri(baseUrl + "/?apikey=" + apiKey + "&i=" + result.getImdbId())
                            .retrieve()
                            .body(OmdbDtos.DetailsResponse.class);

                    if (details != null && "True".equals(details.getResponse())) {
                        ExternalSeriesDto dto = new ExternalSeriesDto();
                        dto.setTitle(details.getTitle());
                        dto.setDescription(!"N/A".equals(details.getPlot()) ? details.getPlot() : null);
                        dto.setStartYear(parseYear(details.getYear()));
                        dto.setPosterUrl(!"N/A".equals(details.getPoster()) ? details.getPoster() : null);

                        dto.setDirector(!"N/A".equals(details.getDirector()) ? details.getDirector() : null);
                        dto.setCastCrew(!"N/A".equals(details.getActors()) ? details.getActors() : null);
                        dto.setContentRating(!"N/A".equals(details.getRated()) ? details.getRated() : null);

                        dto.setTmdbRating(parseRating(details.getImdbRating()));

                        if (details.getGenre() != null && !"N/A".equals(details.getGenre())) {
                            dto.setGenres(Arrays.stream(details.getGenre().split(",")).map(String::trim).toList());
                        }
                        dtos.add(dto);
                    }
                } catch (Exception e) {
                    System.err.println("Error procesando OMDb ID (Series): " + result.getImdbId());
                }
            }
            return dtos;
        } catch (Exception e) {
            return List.of();
        }
    }

    private Integer parseYear(String yearStr) {
        if (yearStr == null || "N/A".equals(yearStr) || yearStr.length() < 4)
            return null;
        try {
            return Integer.parseInt(yearStr.substring(0, 4));
        } catch (Exception e) {
            return null;
        }
    }

    private Integer parseRuntime(String runtimeStr) {
        if (runtimeStr == null || "N/A".equals(runtimeStr))
            return null;
        try {
            return Integer.parseInt(runtimeStr.replace(" min", "").trim());
        } catch (Exception e) {
            return null;
        }
    }

    private Double parseRating(String ratingStr) {
        if (ratingStr == null || "N/A".equals(ratingStr))
            return null;
        try {
            return Double.parseDouble(ratingStr.trim());
        } catch (Exception e) {
            return null;
        }
    }
}