package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MovieMetadataService {

    private final TmdbClient tmdbClient;
    private final OmdbClient omdbClient;

    public ExternalMovieDto fetchMovieData(String title) {
        ExternalMovieDto movie = tmdbClient.searchMovie(title);

        if (movie == null) {
            movie = omdbClient.searchMovie(title);
        }

        return movie;
    }
}