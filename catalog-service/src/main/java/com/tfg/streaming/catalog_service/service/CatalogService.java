package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.*;
import com.tfg.streaming.catalog_service.exception.ResourceNotFoundException;
import com.tfg.streaming.catalog_service.model.Episode;
import com.tfg.streaming.catalog_service.model.Genre;
import com.tfg.streaming.catalog_service.model.Movie;
import com.tfg.streaming.catalog_service.model.Season;
import com.tfg.streaming.catalog_service.model.Series;
import com.tfg.streaming.catalog_service.model.Subtitle;
import com.tfg.streaming.catalog_service.repository.EpisodeRepository;
import com.tfg.streaming.catalog_service.repository.GenreRepository;
import com.tfg.streaming.catalog_service.repository.MovieRepository;
import com.tfg.streaming.catalog_service.repository.ReviewRepository;
import com.tfg.streaming.catalog_service.repository.SeasonRepository;
import com.tfg.streaming.catalog_service.repository.SeriesRepository;
import com.tfg.streaming.catalog_service.repository.SubtitleRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CatalogService {

    private final MovieRepository movieRepository;
    private final SeriesRepository seriesRepository;
    private final SeasonRepository seasonRepository;
    private final EpisodeRepository episodeRepository;
    private final GenreRepository genreRepository;
    private final SubtitleRepository subtitleRepository;
    private final ReviewRepository reviewRepository;
    private final MovieMetadataService movieMetadataService;
    private final RabbitTemplate rabbitTemplate;

    // ==========================================
    // PELICULAS
    // ==========================================
    @CacheEvict(value = { "movies", "genres" }, allEntries = true)
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

    @Cacheable(value = "movies", key = "'page_' + #page + '_size_' + #size")
    public Page<MovieResponse> getAllMovies(int page, int size) {
        return movieRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toMovieResponse);
    }

    @Cacheable(value = "movies", key = "'id_' + #id")
    public MovieResponse getMovieById(Long id) {
        return toMovieResponse(
                movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movie not found")));
    }

    @Cacheable(value = "movies")
    public Page<MovieResponse> searchMovies(String title, int page, int size) {
        return movieRepository
                .findByTitleContainingIgnoreCase(title, PageRequest.of(page, size))
                .map(this::toMovieResponse);
    }

    @Cacheable(value = "movies")
    public Page<MovieResponse> getMoviesByGenre(String genreName, int page, int size) {
        return movieRepository.findByGenresName(genreName, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toMovieResponse);
    }

    @Transactional
    @CacheEvict(value = { "movies", "genres" }, allEntries = true)
    public void deleteMovie(Long id) {
        Movie movie = movieRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Movie not found"));

        deletePhysicalVideo(movie.getVideoPath());
        if (movie.getSubtitles() != null) {
            movie.getSubtitles().forEach(sub -> deletePhysicalVideo(sub.getSubtitlePath()));
        }

        reviewRepository.deleteByMediaIdAndMediaType(id, "movie");
        movieRepository.deleteById(id);

        // Avisar al Users Service para borrar historial y watchlist
        rabbitTemplate.convertAndSend("vodeo.exchange", "media.deleted.key", new MediaDeletedEvent("movie", id));
    }

    @Cacheable(value = "movies", key = "'batch_' + #ids.toString()")
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
    @CacheEvict(value = { "series", "genres" }, allEntries = true)
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

    @Cacheable(value = "series", key = "'page_' + #page + '_size_' + #size")
    public Page<SeriesResponse> getAllSeries(int page, int size) {
        return seriesRepository.findAll(PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toSeriesResponse);
    }

    @Cacheable(value = "series", key = "'id_' + #id")
    public SeriesResponse getSeriesById(Long id) {
        return toSeriesResponse(
                seriesRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Series not found")));
    }

    @Cacheable(value = "series")
    public Page<SeriesResponse> searchSeries(String title, int page, int size) {
        return seriesRepository
                .findByTitleContainingIgnoreCase(title, PageRequest.of(page, size))
                .map(this::toSeriesResponse);
    }

    @Cacheable(value = "series")
    public Page<SeriesResponse> getSeriesByGenre(String genreName, int page, int size) {
        return seriesRepository.findByGenresName(genreName, PageRequest.of(page, size, Sort.by("id").descending()))
                .map(this::toSeriesResponse);
    }

    @Transactional
    @CacheEvict(value = { "series", "genres" }, allEntries = true)
    public void deleteSeries(Long id) {
        Series series = seriesRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found"));

        // Borrar fisicamente los videos de todos los episodios de todas las temporadas
        series.getSeasons().forEach(season -> season.getEpisodes().forEach(ep -> {
            deletePhysicalVideo(ep.getVideoPath());
            if (ep.getSubtitles() != null) {
                ep.getSubtitles().forEach(sub -> deletePhysicalVideo(sub.getSubtitlePath()));
            }
        }));

        reviewRepository.deleteByMediaIdAndMediaType(id, "series");
        seriesRepository.deleteById(id);

        // Avisar al Users Service
        rabbitTemplate.convertAndSend("vodeo.exchange", "media.deleted.key", new MediaDeletedEvent("series", id));
    }

    @Cacheable(value = "series", key = "'batch_' + #ids.toString()")
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
    @Cacheable(value = "series", key = "'seasons_series_' + #seriesId")
    public List<SeasonResponse> getSeasonsBySeriesId(Long seriesId) {
        return seasonRepository.findBySeriesIdOrderBySeasonNumberAsc(seriesId).stream()
                .map(this::toSeasonResponse).toList();
    }

    @CacheEvict(value = "series", allEntries = true)
    public SeasonResponse addSeason(Long seriesId, SeasonRequest request) {
        Series series = seriesRepository.findById(seriesId)
                .orElseThrow(() -> new ResourceNotFoundException("Series not found"));
        Season season = new Season();
        season.setSeasonNumber(request.getSeasonNumber());
        season.setOverview(request.getOverview());
        season.setSeries(series);
        return toSeasonResponse(seasonRepository.save(season));
    }

    @CacheEvict(value = "series", allEntries = true)
    public EpisodeResponse addEpisode(Long seasonId, EpisodeRequest request) {
        Season season = seasonRepository.findById(seasonId)
                .orElseThrow(() -> new ResourceNotFoundException("Season not found"));
        Episode ep = new Episode();
        ep.setEpisodeNumber(request.getEpisodeNumber());
        ep.setTitle(request.getTitle());
        ep.setDescription(request.getDescription());
        ep.setDuration(request.getDuration());
        ep.setVideoPath(request.getVideoPath());
        ep.setSeason(season);
        return toEpisodeResponse(episodeRepository.save(ep));
    }

    @CacheEvict(value = "series", allEntries = true)
    public void deleteEpisode(Long id) {
        Episode ep = episodeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found"));

        deletePhysicalVideo(ep.getVideoPath());
        if (ep.getSubtitles() != null) {
            ep.getSubtitles().forEach(sub -> deletePhysicalVideo(sub.getSubtitlePath()));
        }

        episodeRepository.deleteById(id);

        // Avisar al Users Service
        rabbitTemplate.convertAndSend("vodeo.exchange", "media.deleted.key", new MediaDeletedEvent("episode", id));
    }

    @Cacheable(value = "series", key = "'ep_' + #id")
    public EpisodeResponse getEpisodeById(Long id) {
        return toEpisodeResponse(
                episodeRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Episode not found")));
    }

    @Cacheable(value = "series", key = "'next_ep_' + #currentEpisodeId")
    public EpisodeResponse getNextEpisode(Long currentEpisodeId) {
        Episode currentEp = episodeRepository.findById(currentEpisodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episode not found"));

        // Intentar buscar el siguiente episodio en la MISMA temporada
        Optional<Episode> nextInSeason = episodeRepository
                .findFirstBySeasonIdAndEpisodeNumberGreaterThanOrderByEpisodeNumberAsc(
                        currentEp.getSeason().getId(), currentEp.getEpisodeNumber());

        if (nextInSeason.isPresent()) {
            return toEpisodeResponse(nextInSeason.get());
        }

        // Si no hay más en esta temporada, buscar la SIGUIENTE temporada de la
        // serie
        Optional<Season> nextSeason = seasonRepository
                .findFirstBySeriesIdAndSeasonNumberGreaterThanOrderBySeasonNumberAsc(
                        currentEp.getSeason().getSeries().getId(), currentEp.getSeason().getSeasonNumber());

        if (nextSeason.isPresent()) {
            // Devolver el primer episodio de esa nueva temporada
            Optional<Episode> firstEpNextSeason = episodeRepository
                    .findFirstBySeasonIdOrderByEpisodeNumberAsc(nextSeason.get().getId());
            if (firstEpNextSeason.isPresent()) {
                return toEpisodeResponse(firstEpNextSeason.get());
            }
        }

        // Ha visto el último episodio de la última temporada por lo que fin de la serie
        return null;
    }

    // ==========================================
    // MAPPERS Y UTILS
    // ==========================================
    @Cacheable(value = "genres")
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
        if (videoPath != null && !videoPath.trim().isEmpty()) {
            // Mandamos el mensaje por RabbitMQ. Si el streaming-service esta caido Rabbit
            // se lo guarda
            rabbitTemplate.convertAndSend("vodeo.exchange", "file.delete.key", videoPath);
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

        if (movie.getSubtitles() != null) {
            response.setSubtitles(movie.getSubtitles().stream().map(sub -> {
                SubtitleResponse subRes = new SubtitleResponse();
                subRes.setId(sub.getId());
                subRes.setLanguage(sub.getLanguage());
                subRes.setLanguageCode(sub.getLanguageCode());
                subRes.setSubtitlePath(sub.getSubtitlePath());
                return subRes;
            }).toList());
        }

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

        // EXTRAEMOS LOS DATOS DE LA SERIE PADRE
        if (episode.getSeason() != null && episode.getSeason().getSeries() != null) {
            res.setSeriesTitle(episode.getSeason().getSeries().getTitle());
            res.setPosterUrl(episode.getSeason().getSeries().getPosterUrl());

            res.setSeriesId(episode.getSeason().getSeries().getId());
            if (episode.getSeason().getSeries().getGenres() != null) {
                res.setSeriesGenres(episode.getSeason().getSeries().getGenres().stream()
                        .map(Genre::getName)
                        .toList());
            }
        }

        if (episode.getSubtitles() != null) {
            res.setSubtitles(episode.getSubtitles().stream().map(sub -> {
                SubtitleResponse subRes = new SubtitleResponse();
                subRes.setId(sub.getId());
                subRes.setLanguage(sub.getLanguage());
                subRes.setLanguageCode(sub.getLanguageCode());
                subRes.setSubtitlePath(sub.getSubtitlePath());
                return subRes;
            }).toList());
        }

        return res;
    }

    // ==========================================
    // SUBTÍTULOS
    // ==========================================
    @CacheEvict(value = { "movies", "genres" }, allEntries = true)
    public SubtitleResponse addSubtitleToMovie(Long movieId, SubtitleRequest request) {
        Movie movie = movieRepository.findById(movieId)
                .orElseThrow(() -> new ResourceNotFoundException("Película no encontrada"));

        Subtitle subtitle = new Subtitle();
        subtitle.setLanguage(request.getLanguage());
        subtitle.setLanguageCode(request.getLanguageCode());
        subtitle.setSubtitlePath(request.getSubtitlePath());
        subtitle.setMovie(movie);

        return toSubtitleResponse(subtitleRepository.save(subtitle));
    }

    @CacheEvict(value = { "series", "genres" }, allEntries = true)
    public SubtitleResponse addSubtitleToEpisode(Long episodeId, SubtitleRequest request) {
        Episode episode = episodeRepository.findById(episodeId)
                .orElseThrow(() -> new ResourceNotFoundException("Episodio no encontrado"));

        Subtitle subtitle = new Subtitle();
        subtitle.setLanguage(request.getLanguage());
        subtitle.setLanguageCode(request.getLanguageCode());
        subtitle.setSubtitlePath(request.getSubtitlePath());
        subtitle.setEpisode(episode);

        return toSubtitleResponse(subtitleRepository.save(subtitle));
    }

    @CacheEvict(value = { "movies", "series", "genres" }, allEntries = true)
    public void deleteSubtitle(Long id) {
        Subtitle subtitle = subtitleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Subtítulo no encontrado"));
        deletePhysicalVideo(subtitle.getSubtitlePath());
        subtitleRepository.deleteById(id);
    }

    private SubtitleResponse toSubtitleResponse(Subtitle sub) {
        SubtitleResponse res = new SubtitleResponse();
        res.setId(sub.getId());
        res.setLanguage(sub.getLanguage());
        res.setLanguageCode(sub.getLanguageCode());
        res.setSubtitlePath(sub.getSubtitlePath());
        return res;
    }
}