package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MovieMetadataService {

    private final TmdbClient tmdbClient;
    private final OmdbClient omdbClient;

    public List<ExternalMovieDto> fetchMovieData(String title) {
        List<ExternalMovieDto> movies = tmdbClient.searchMovies(title);

        if (movies == null || movies.isEmpty()) {
            movies = omdbClient.searchMovies(title);
        }

        return movies;
    }
}