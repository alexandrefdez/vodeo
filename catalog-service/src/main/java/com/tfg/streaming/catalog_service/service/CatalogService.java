package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.MovieRequest;
import com.tfg.streaming.catalog_service.dto.MovieResponse;
import com.tfg.streaming.catalog_service.model.Genre;
import com.tfg.streaming.catalog_service.model.Movie;
import com.tfg.streaming.catalog_service.repository.GenreRepository;
import com.tfg.streaming.catalog_service.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final TmdbClient tmdbClient;

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setYear(request.getYear());
        movie.setDuration(request.getDuration());

        List<Genre> genres = genreRepository.findAllById(request.getGenreIds());
        movie.setGenres(genres);

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    public List<MovieResponse> getAllMovies() {
        return movieRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return toResponse(movie);
    }

    public List<MovieResponse> searchMovies(String title) {
        return movieRepository.findByTitleContainingIgnoreCase(title)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public void deleteMovie(Long id) {
        movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        movieRepository.deleteById(id);
    }

    private MovieResponse toResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setYear(movie.getYear());
        response.setDuration(movie.getDuration());
        response.setVideoPath(movie.getVideoPath());
        response.setGenres(movie.getGenres().stream()
                .map(Genre::getName)
                .toList());
        return response;
    }

    public ExternalMovieDto fetchMovieData(String title) {
        return tmdbClient.searchMovie(title);
    }
}