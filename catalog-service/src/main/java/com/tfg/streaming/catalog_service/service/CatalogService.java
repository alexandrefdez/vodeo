package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.*;
import com.tfg.streaming.catalog_service.model.Episode;
import com.tfg.streaming.catalog_service.model.Genre;
import com.tfg.streaming.catalog_service.model.Movie;
import com.tfg.streaming.catalog_service.model.Season;
import com.tfg.streaming.catalog_service.model.Series;
import com.tfg.streaming.catalog_service.repository.EpisodeRepository;
import com.tfg.streaming.catalog_service.repository.GenreRepository;
import com.tfg.streaming.catalog_service.repository.MovieRepository;
import com.tfg.streaming.catalog_service.repository.SeasonRepository;
import com.tfg.streaming.catalog_service.repository.SeriesRepository;
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
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final GenreRepository genreRepository;
    private final MovieMetadataService movieMetadataService;

    @Value("${streaming-service.url:http://localhost:8083/api/streaming}")
    private String streamingServiceUrl;

    private final RestClient restClient = RestClient.create();

    // ==========================================
    // PELICULAS
    // ==========================================
    public MovieResponse createMovie(MovieRequest request) {
        Movie movie = new Movie();
        movie.setTitle(request.getTitle());
        movie.setDescription(request.getDescription());
        movie.setYear(request.getYear());
        movie.setDuration(request.getDuration());
        movie.setPosterUrl(request.getPosterUrl());
        movie.setVideoPath(request.getVideoPath());
        movie.setBackdropUrl(request.getBackdropUrl());
        movie.setTmdbRating(request.getTmdbRating());
        movie.setContentRating(request.getContentRating());
        movie.setDirector(request.getDirector());
        movie.setCastCrew(request.getCastCrew());
        movie.setGenres(processGenres(request.getGenres()));
        return toMovieResponse(movieRepository.save(movie));
    }

    public Page<MovieResponse> getAllMovies(int page, int size) {
        return movieRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toMovieResponse);
    }

    public MovieResponse getMovieById(Long id) {
        return toMovieResponse(movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found")));
    }

    public Page<MovieResponse> searchMovies(String title, int page, int size) {
        return movieRepository
                .findByTitleContainingIgnoreCase(title, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toMovieResponse);
    }

    public Page<MovieResponse> getMoviesByGenre(String genreName, int page, int size) {
        return movieRepository.findByGenresName(genreName, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toMovieResponse);
    }

    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new RuntimeException("Movie not found"));
        deletePhysicalVideo(movie.getVideoPath());
        movieRepository.deleteById(id);
    }

    public List<MovieResponse> getMoviesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return movieRepository.findByIdIn(ids).stream().map(this::toMovieResponse).toList();
    }

    public List<ExternalMovieDto> fetchMovieData(String title) {
        return movieMetadataService.fetchMovieData(title);
    }

    // ==========================================
    // SERIES
    // ==========================================
    public SeriesResponse createSeries(SeriesRequest request) {
        Series series = new Series();
        series.setTitle(request.getTitle());
        series.setDescription(request.getDescription());
        series.setStartYear(request.getStartYear());
        series.setPosterUrl(request.getPosterUrl());
        series.setBackdropUrl(request.getBackdropUrl());
        series.setTmdbRating(request.getTmdbRating());
        series.setContentRating(request.getContentRating());
        series.setDirector(request.getDirector());
        series.setCastCrew(request.getCastCrew());
        series.setGenres(processGenres(request.getGenres()));
        return toSeriesResponse(seriesRepository.save(series));
    }

    public Page<SeriesResponse> getAllSeries(int page, int size) {
        return seriesRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toSeriesResponse);
    }

    public SeriesResponse getSeriesById(Long id) {
        return toSeriesResponse(
                seriesRepository.findById(id).orElseThrow(() -> new RuntimeException("Series not found")));
    }

    public Page<SeriesResponse> searchSeries(String title, int page, int size) {
        return seriesRepository
                .findByTitleContainingIgnoreCase(title, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toSeriesResponse);
    }

    public Page<SeriesResponse> getSeriesByGenre(String genreName, int page, int size) {
        return seriesRepository.findByGenresName(genreName, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toSeriesResponse);
    }

    public void deleteSeries(Long id) {
        // En una app real iterarías sobre los episodios para borrar los .mp4 físicos,
        // aquí delegamos el borrado en cascada de BBDD
        seriesRepository.deleteById(id);
    }

    public List<SeriesResponse> getSeriesByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty())
            return List.of();
        return seriesRepository.findByIdIn(ids).stream().map(this::toSeriesResponse).toList();
    }

    public List<ExternalSeriesDto> fetchSeriesData(String title) {
        return movieMetadataService.fetchSeriesData(title);
    }

    // ==========================================
    // GESTOR DE TEMPORADAS Y EPISODIOS
    // ==========================================
    public List<SeasonResponse> getSeasonsBySeriesId(Long seriesId) {
        return seasonRepository.findBySeriesIdOrderBySeasonNumberAsc(seriesId).stream()
                .map(this::toSeasonResponse).toList();
    }

    public SeasonResponse addSeason(Long seriesId, SeasonRequest request) {
        Series series = seriesRepository.findById(seriesId).orElseThrow(() -> new RuntimeException("Series not found"));
        Season season = new Season();
        season.setSeasonNumber(request.getSeasonNumber());
        season.setOverview(request.getOverview());
        season.setSeries(series);
        return toSeasonResponse(seasonRepository.save(season));
    }

    public EpisodeResponse addEpisode(Long seasonId, EpisodeRequest request) {
        Season season = seasonRepository.findById(seasonId).orElseThrow(() -> new RuntimeException("Season not found"));
        Episode ep = new Episode();
        ep.setEpisodeNumber(request.getEpisodeNumber());
        ep.setTitle(request.getTitle());
        ep.setDescription(request.getDescription());
        ep.setDuration(request.getDuration());
        ep.setVideoPath(request.getVideoPath());
        ep.setSeason(season);
        return toEpisodeResponse(episodeRepository.save(ep));
    }

    public void deleteEpisode(Long id) {
        Episode ep = episodeRepository.findById(id).orElseThrow(() -> new RuntimeException("Episode not found"));
        deletePhysicalVideo(ep.getVideoPath());
        episodeRepository.deleteById(id);
    }

    public EpisodeResponse getEpisodeById(Long id) {
        return toEpisodeResponse(
                episodeRepository.findById(id).orElseThrow(() -> new RuntimeException("Episode not found")));
    }

    // ==========================================
    // MAPPERS Y UTILS
    // ==========================================
    public List<String> getActiveGenres() {
        return genreRepository.findActiveGenres();
    }

    private List<Genre> processGenres(List<String> genreNames) {
        List<Genre> genres = new ArrayList<>();
        if (genreNames != null) {
            for (String name : genreNames) {
                genres.add(genreRepository.findByName(name).orElseGet(() -> {
                    Genre newGenre = new Genre();
                    newGenre.setName(name);
                    return genreRepository.save(newGenre);
                }));
            }
        }
        return genres;
    }

    private void deletePhysicalVideo(String videoPath) {
        if (videoPath != null && !videoPath.isEmpty()) {
            try {
                ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder
                        .getRequestAttributes();
                String token = attributes != null ? attributes.getRequest().getHeader("Authorization") : "";
                restClient.delete()
                        .uri(streamingServiceUrl + "/video?videoPath=" + videoPath)
                        .header("Authorization", token)
                        .retrieve()
                        .toBodilessEntity();
            } catch (Exception e) {
                System.err.println("Advertencia: No se pudo borrar el archivo físico.");
            }
        }
    }

    public ExternalEpisodeDto fetchEpisodeData(String seriesTitle, Integer seasonNumber, Integer episodeNumber) {
        return movieMetadataService.fetchEpisodeData(seriesTitle, seasonNumber, episodeNumber);
    }

    private MovieResponse toMovieResponse(Movie movie) {
        MovieResponse response = new MovieResponse();
        response.setId(movie.getId());
        response.setTitle(movie.getTitle());
        response.setDescription(movie.getDescription());
        response.setYear(movie.getYear());
        response.setDuration(movie.getDuration());
        response.setVideoPath(movie.getVideoPath());
        response.setPosterUrl(movie.getPosterUrl());
        response.setBackdropUrl(movie.getBackdropUrl());
        response.setTmdbRating(movie.getTmdbRating());
        response.setContentRating(movie.getContentRating());
        response.setDirector(movie.getDirector());
        response.setCastCrew(movie.getCastCrew());
        response.setGenres(movie.getGenres().stream().map(Genre::getName).toList());
        return response;
    }

    private SeriesResponse toSeriesResponse(Series series) {
        SeriesResponse response = new SeriesResponse();
        response.setId(series.getId());
        response.setTitle(series.getTitle());
        response.setDescription(series.getDescription());
        response.setStartYear(series.getStartYear());
        response.setPosterUrl(series.getPosterUrl());
        response.setBackdropUrl(series.getBackdropUrl());
        response.setTmdbRating(series.getTmdbRating());
        response.setContentRating(series.getContentRating());
        response.setDirector(series.getDirector());
        response.setCastCrew(series.getCastCrew());
        response.setGenres(series.getGenres().stream().map(Genre::getName).toList());
        return response;
    }

    private SeasonResponse toSeasonResponse(Season season) {
        SeasonResponse res = new SeasonResponse();
        res.setId(season.getId());
        res.setSeasonNumber(season.getSeasonNumber());
        res.setOverview(season.getOverview());
        if (season.getEpisodes() != null) {
            res.setEpisodes(season.getEpisodes().stream()
                    .map(this::toEpisodeResponse)
                    .sorted(Comparator.comparing(EpisodeResponse::getEpisodeNumber))
                    .toList());
        } else {
            res.setEpisodes(new ArrayList<>());
        }
        return res;
    }

    private EpisodeResponse toEpisodeResponse(Episode episode) {
        EpisodeResponse res = new EpisodeResponse();
        res.setId(episode.getId());
        res.setEpisodeNumber(episode.getEpisodeNumber());
        res.setTitle(episode.getTitle());
        res.setDescription(episode.getDescription());
        res.setDuration(episode.getDuration());
        res.setVideoPath(episode.getVideoPath());
        res.setSeasonId(episode.getSeason().getId());
        return res;
    }
}