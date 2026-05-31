package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.MovieRequest;
import com.tfg.streaming.catalog_service.dto.MovieResponse;
import com.tfg.streaming.catalog_service.model.Genre;
import com.tfg.streaming.catalog_service.model.Movie;
import com.tfg.streaming.catalog_service.repository.GenreRepository;
import com.tfg.streaming.catalog_service.repository.MovieRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MovieRepository movieRepository;
    private final GenreRepository genreRepository;
    private final MovieMetadataService movieMetadataService;

    @Value("${streaming-service.url:http://localhost:8083/api/streaming}")
    private String streamingServiceUrl;

    private final RestClient restClient = RestClient.create();

    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setYear(request.getYear());
        movie.setDuration(request.getDuration());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setVideoPath(request.getVideoPath());

        List<Genre> genres = new ArrayList<>();
        if (request.getGenres() != null) {
            for (String genreName : request.getGenres()) {
                Genre genre = genreRepository.findByName(genreName)
                        .orElseGet(() -> {
                            Genre newGenre = new Genre();
                            newGenre.setName(genreName);
                            return genreRepository.save(newGenre);
                        });
                genres.add(genre);
            }
        }
        movie.setGenres(genres);

        Movie saved = movieRepository.save(movie);
        return toResponse(saved);
    }

    public Page<MovieResponse> getAllMovies(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return movieRepository.findAll(pageable)
                .map(this::toResponse);
    }

    public MovieResponse getMovieById(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));
        return toResponse(movie);
    }

    public Page<MovieResponse> searchMovies(String title, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return movieRepository.findByTitleContainingIgnoreCase(title, pageable)
                .map(this::toResponse);
    }

    public Page<MovieResponse> getMoviesByGenre(String genreName, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return movieRepository.findByGenresName(genreName, pageable)
                .map(this::toResponse);
    }

    public List<String> getActiveGenres() {
        return genreRepository.findActiveGenres();
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Movie not found"));

        if (movie.getVideoPath() != null && !movie.getVideoPath().isEmpty()) {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                String token = attributes != null ? attributes.getRequest().getHeader("Authorization") : "";

                restClient.delete()
                        .uri(streamingServiceUrl + "/video?videoPath=" + movie.getVideoPath())
                        .header("Authorization", token)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo borrar el archivo físico del servidor de streaming.");
            }
        }

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
        response.setPosterUrl(movie.getPosterUrl());
        response.setGenres(movie.getGenres().stream()
                .map(Genre::getName)
                .toList());
        return response;
    }

    public ExternalMovieDto fetchMovieData(String title) {
        return movieMetadataService.fetchMovieData(title);
    }
}