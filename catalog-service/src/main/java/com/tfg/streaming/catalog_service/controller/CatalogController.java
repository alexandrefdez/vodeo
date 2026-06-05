package com.tfg.streaming.catalog_service.controller;

import com.tfg.streaming.catalog_service.dto.*;
import com.tfg.streaming.catalog_service.service.CatalogService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.tfg.streaming.catalog_service.dto.SubtitleRequest;
import com.tfg.streaming.catalog_service.dto.SubtitleResponse;

import java.util.List;

@RestController
@RequestMapping("/api/catalog")
@RequiredArgsConstructor
public class CatalogController {

    private final CatalogService catalogService;

    // ==========================================
    // MOVIES
    // ==========================================

    @PostMapping("/movies")
    public ResponseEntity<MovieResponse> createMovie(@RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createMovie(request));
    }

    @GetMapping("/movies")
    public ResponseEntity<Page<MovieResponse>> getAllMovies(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.getAllMovies(page, size));
    }

    @GetMapping("/movies/{id}")
    public ResponseEntity<MovieResponse> getMovieById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getMovieById(id));
    }

    @GetMapping("/movies/search")
    public ResponseEntity<Page<MovieResponse>> searchMovies(@RequestParam String title,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.searchMovies(title, page, size));
    }

    @GetMapping("/movies/genre")
    public ResponseEntity<Page<MovieResponse>> getMoviesByGenre(@RequestParam String genre,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.getMoviesByGenre(genre, page, size));
    }

    @DeleteMapping("/movies/{id}")
    public ResponseEntity<Void> deleteMovie(@PathVariable Long id) {
        catalogService.deleteMovie(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/movies/fetch")
    public ResponseEntity<List<ExternalMovieDto>> fetchMovieData(@RequestParam String title) {
        List<ExternalMovieDto> data = catalogService.fetchMovieData(title);
        if (data == null || data.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/movies/batch")
    public ResponseEntity<List<MovieResponse>> getMoviesByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(catalogService.getMoviesByIds(ids));
    }

    // ==========================================
    // SERIES
    // ==========================================

    @PostMapping("/series")
    public ResponseEntity<SeriesResponse> createSeries(@RequestBody SeriesRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.createSeries(request));
    }

    @GetMapping("/series")
    public ResponseEntity<Page<SeriesResponse>> getAllSeries(@RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.getAllSeries(page, size));
    }

    @GetMapping("/series/{id}")
    public ResponseEntity<SeriesResponse> getSeriesById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getSeriesById(id));
    }

    @GetMapping("/series/search")
    public ResponseEntity<Page<SeriesResponse>> searchSeries(@RequestParam String title,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.searchSeries(title, page, size));
    }

    @GetMapping("/series/genre")
    public ResponseEntity<Page<SeriesResponse>> getSeriesByGenre(@RequestParam String genre,
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(catalogService.getSeriesByGenre(genre, page, size));
    }

    @DeleteMapping("/series/{id}")
    public ResponseEntity<Void> deleteSeries(@PathVariable Long id) {
        catalogService.deleteSeries(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/series/fetch")
    public ResponseEntity<List<ExternalSeriesDto>> fetchSeriesData(@RequestParam String title) {
        List<ExternalSeriesDto> data = catalogService.fetchSeriesData(title);
        if (data == null || data.isEmpty())
            return ResponseEntity.notFound().build();
        return ResponseEntity.ok(data);
    }

    @GetMapping("/series/batch")
    public ResponseEntity<List<SeriesResponse>> getSeriesByIds(@RequestParam List<Long> ids) {
        return ResponseEntity.ok(catalogService.getSeriesByIds(ids));
    }

    // ==========================================
    // TEMPORADAS Y EPISODIOS
    // ==========================================
    @GetMapping("/series/{id}/seasons")
    public ResponseEntity<List<SeasonResponse>> getSeasons(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getSeasonsBySeriesId(id));
    }

    @PostMapping("/series/{id}/seasons")
    public ResponseEntity<SeasonResponse> addSeason(@PathVariable Long id, @RequestBody SeasonRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addSeason(id, request));
    }

    @PostMapping("/seasons/{id}/episodes")
    public ResponseEntity<EpisodeResponse> addEpisode(@PathVariable Long id, @RequestBody EpisodeRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addEpisode(id, request));
    }

    @GetMapping("/episodes/{id}")
    public ResponseEntity<EpisodeResponse> getEpisodeById(@PathVariable Long id) {
        return ResponseEntity.ok(catalogService.getEpisodeById(id));
    }

    @DeleteMapping("/episodes/{id}")
    public ResponseEntity<Void> deleteEpisode(@PathVariable Long id) {
        catalogService.deleteEpisode(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/series/fetch-episode")
    public ResponseEntity<ExternalEpisodeDto> fetchEpisodeData(
            @RequestParam String seriesTitle,
            @RequestParam Integer seasonNumber,
            @RequestParam Integer episodeNumber) {
        ExternalEpisodeDto data = catalogService.fetchEpisodeData(seriesTitle, seasonNumber, episodeNumber);
        if (data == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(data);
    }

    @GetMapping("/episodes/{id}/next")
    public ResponseEntity<EpisodeResponse> getNextEpisode(@PathVariable Long id) {
        EpisodeResponse nextEpisode = catalogService.getNextEpisode(id);

        if (nextEpisode == null) {
            // Devolver 204 No Content si la serie se ha terminado
            return ResponseEntity.noContent().build();
        }

        return ResponseEntity.ok(nextEpisode);
    }

    // ==========================================
    // SUBTÍTULOS
    // ==========================================
    @PostMapping("/movies/{id}/subtitles")
    public ResponseEntity<SubtitleResponse> addSubtitleToMovie(@PathVariable Long id,
            @RequestBody SubtitleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addSubtitleToMovie(id, request));
    }

    @PostMapping("/episodes/{id}/subtitles")
    public ResponseEntity<SubtitleResponse> addSubtitleToEpisode(@PathVariable Long id,
            @RequestBody SubtitleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(catalogService.addSubtitleToEpisode(id, request));
    }

    @DeleteMapping("/subtitles/{id}")
    public ResponseEntity<Void> deleteSubtitle(@PathVariable Long id) {
        catalogService.deleteSubtitle(id);
        return ResponseEntity.noContent().build();
    }

    // ==========================================
    // GLOBAL
    // ==========================================

    @GetMapping("/genres")
    public ResponseEntity<List<String>> getActiveGenres() {
        return ResponseEntity.ok(catalogService.getActiveGenres());
    }
}