package com.tfg.streaming.catalog_service.controller;

import com.tfg.streaming.catalog_service.dto.MovieRequest;
import com.tfg.streaming.catalog_service.dto.MovieResponse;
import com.tfg.streaming.catalog_service.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(catalogService.getAllMovies());
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getMovieById(id));
    }

    @GetMapping("/movies/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(@RequestParam String title) {
        return ResponseEntity.ok(catalogService.searchMovies(title));
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        catalogService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }
}