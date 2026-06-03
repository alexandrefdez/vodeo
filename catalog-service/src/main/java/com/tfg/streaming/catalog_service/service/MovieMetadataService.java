package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalEpisodeDto;
import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.ExternalSeriesDto;
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

    public List<ExternalSeriesDto> fetchSeriesData(String title) {
        List<ExternalSeriesDto> series = tmdbClient.searchSeries(title);

        if (series == null || series.isEmpty()) {
            series = omdbClient.searchSeries(title);
        }
        return series;
    }

    public ExternalEpisodeDto fetchEpisodeData(String seriesTitle, Integer seasonNumber, Integer episodeNumber) {
        return tmdbClient.fetchEpisodeData(seriesTitle, seasonNumber, episodeNumber);
    }
}