package com.tfg.streaming.catalog_service.controller;

import com.tfg.streaming.catalog_service.dto.ExternalMovieDto;
import com.tfg.streaming.catalog_service.dto.MovieRequest;
import com.tfg.streaming.catalog_service.dto.MovieResponse;
import com.tfg.streaming.catalog_service.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    @PostMapping("/movies")
    public ResponseEntity<MovieResponse> createMovie(@RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createMovie(request));
    }

    @GetMapping("/movies")
    public ResponseEntity<Page<MovieResponse>> getAllMovies(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.getAllMovies(page, size));
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getMovieById(id));
    }

    @GetMapping("/movies/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(
            @RequestParam String title,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.searchMovies(title, page, size));
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        catalogService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movies/fetch")
    public ResponseEntity<ExternalMovieDto> fetchMovieData(@RequestParam String title) {
        ExternalMovieDto data = catalogService.fetchMovieData(title);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }
}