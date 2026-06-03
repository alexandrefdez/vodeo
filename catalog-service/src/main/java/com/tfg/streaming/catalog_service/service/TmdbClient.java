package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TmdbClient {

    @Value("${tmdb.api-key}")
    private String apiKey;
    @Value("${tmdb.base-url}")
    private String baseUrl;
    @Value("${tmdb.image-base-url}")
    private String imageBaseUrl;

    private final RestClient restClient = RestClient.create();

    public List<ExternalMovieDto> searchMovies(String title) {
        try {
            TmdbSearchResponse response = restClient.get()
                    .uri(baseUrl + "/search/movie?api_key=" + apiKey + "&query=" + title + "&language=es-ES")
                    .retrieve()
                    .body(TmdbSearchResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty())
                return List.of();

            List<ExternalMovieDto> dtos = new ArrayList<>();
            List<TmdbMovieResult> topResults = response.getResults().stream().limit(5).toList();

            for (TmdbMovieResult result : topResults) {
                ExternalMovieDto dto = new ExternalMovieDto();
                dto.setTitle(result.getTitle());
                dto.setDescription(result.getOverview());
                dto.setYear(parseYear(result.getReleaseDate()));
                dto.setPosterUrl(result.getPosterPath() != null ? imageBaseUrl + result.getPosterPath() : null);

                try {
                    TmdbMovieDetails details = restClient.get()
                            .uri(baseUrl + "/movie/" + result.getId() + "?api_key=" + apiKey
                                    + "&language=es-ES&append_to_response=credits,release_dates")
                            .retrieve()
                            .body(TmdbMovieDetails.class);

                    if (details != null) {
                        dto.setDuration(details.getRuntime());
                        dto.setTmdbRating(details.getVoteAverage());
                        dto.setBackdropUrl(
                                details.getBackdropPath() != null ? imageBaseUrl + details.getBackdropPath() : null);

                        if (details.getGenres() != null) {
                            dto.setGenres(details.getGenres().stream().map(TmdbMovieDetails.Genre::getName).toList());
                        }

                        if (details.getCredits() != null && details.getCredits().getCrew() != null) {
                            dto.setDirector(details.getCredits().getCrew().stream()
                                    .filter(c -> "Director".equals(c.getJob()))
                                    .map(TmdbMovieDetails.Crew::getName).findFirst().orElse("Desconocido"));
                        }

                        if (details.getCredits() != null && details.getCredits().getCast() != null) {
                            dto.setCastCrew(details.getCredits().getCast().stream()
                                    .limit(5).map(TmdbMovieDetails.Cast::getName).collect(Collectors.joining(", ")));
                        }

                        if (details.getReleaseDates() != null && details.getReleaseDates().getResults() != null) {
                            dto.setContentRating(details.getReleaseDates().getResults().stream()
                                    .filter(r -> "US".equals(r.getIso31661()) || "ES".equals(r.getIso31661()))
                                    .flatMap(r -> r.getReleaseDates().stream())
                                    .map(TmdbMovieDetails.ReleaseDate::getCertification)
                                    .filter(c -> c != null && !c.isEmpty()).findFirst().orElse("NR"));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error en detalle TMDB (Peli): " + e.getMessage());
                }
                dtos.add(dto);
            }
            return dtos;
        } catch (Exception e) {
            return List.of();
        }
    }

    public List<ExternalSeriesDto> searchSeries(String title) {
        try {
            TmdbSeriesSearchResponse response = restClient.get()
                    .uri(baseUrl + "/search/tv?api_key=" + apiKey + "&query=" + title + "&language=es-ES")
                    .retrieve()
                    .body(TmdbSeriesSearchResponse.class);

            if (response == null || response.getResults() == null || response.getResults().isEmpty())
                return List.of();

            List<ExternalSeriesDto> dtos = new ArrayList<>();
            List<TmdbSeriesResult> topResults = response.getResults().stream().limit(5).toList();

            for (TmdbSeriesResult result : topResults) {
                ExternalSeriesDto dto = new ExternalSeriesDto();
                dto.setTitle(result.getName());
                dto.setDescription(result.getOverview());
                dto.setStartYear(parseYear(result.getFirstAirDate()));
                dto.setPosterUrl(result.getPosterPath() != null ? imageBaseUrl + result.getPosterPath() : null);

                try {
                    TmdbSeriesDetails details = restClient.get()
                            .uri(baseUrl + "/tv/" + result.getId() + "?api_key=" + apiKey
                                    + "&language=es-ES&append_to_response=credits,content_ratings")
                            .retrieve()
                            .body(TmdbSeriesDetails.class);

                    if (details != null) {
                        dto.setTmdbRating(details.getVoteAverage());
                        dto.setBackdropUrl(
                                details.getBackdropPath() != null ? imageBaseUrl + details.getBackdropPath() : null);

                        if (details.getGenres() != null) {
                            dto.setGenres(details.getGenres().stream().map(TmdbMovieDetails.Genre::getName).toList());
                        }

                        // Extraemos de 'created_by'
                        if (details.getCreatedBy() != null && !details.getCreatedBy().isEmpty()) {
                            dto.setDirector(details.getCreatedBy().stream()
                                    .map(TmdbSeriesDetails.Creator::getName).collect(Collectors.joining(", ")));
                        } else {
                            dto.setDirector("Desconocido");
                        }

                        if (details.getCredits() != null && details.getCredits().getCast() != null) {
                            dto.setCastCrew(details.getCredits().getCast().stream()
                                    .limit(5).map(TmdbMovieDetails.Cast::getName).collect(Collectors.joining(", ")));
                        }

                        // Extraemos de 'content_ratings'
                        if (details.getContentRatings() != null && details.getContentRatings().getResults() != null) {
                            dto.setContentRating(details.getContentRatings().getResults().stream()
                                    .filter(r -> "US".equals(r.getIso31661()) || "ES".equals(r.getIso31661()))
                                    .map(TmdbSeriesDetails.RatingResult::getRating)
                                    .filter(c -> c != null && !c.isEmpty()).findFirst().orElse("NR"));
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Error en detalle TMDB (Serie): " + e.getMessage());
                }
                dtos.add(dto);
            }
            return dtos;

        } catch (Exception e) {
            return List.of();
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