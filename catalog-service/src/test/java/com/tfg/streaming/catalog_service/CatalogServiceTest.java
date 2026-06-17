package com.tfg.streaming.catalog_service;

import com.tfg.streaming.catalog_service.dto.*;
import com.tfg.streaming.catalog_service.exception.ResourceNotFoundException;
import com.tfg.streaming.catalog_service.model.*;
import com.tfg.streaming.catalog_service.repository.*;
import com.tfg.streaming.catalog_service.service.CatalogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CatalogService — Tests Unitarios")
class CatalogServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @Mock
    private SeriesRepository seriesRepository;
    @Mock
    private EpisodeRepository episodeRepository;
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private CatalogService catalogService;

    private Movie mockMovie;
    private Series mockSeries;

    @BeforeEach
    void setUp() {
        Genre genre = new Genre();
        genre.setId(1L);
        genre.setName("Action");

        mockMovie = new Movie();
        mockMovie.setId(99L);
        mockMovie.setTitle("Inception");
        mockMovie.setYear(2010);
        mockMovie.setDuration(148);
        mockMovie.setGenres(new ArrayList<>(List.of(genre)));

        mockSeries = new Series();
        mockSeries.setId(10L);
        mockSeries.setTitle("Breaking Bad");
    }

    @Test
    @DisplayName("getMovieById — película existente devuelve DTO correcto")
    void testGetMovieById_Success() {
        when(movieRepository.findById(99L)).thenReturn(Optional.of(mockMovie));
        MovieResponse response = catalogService.getMovieById(99L);
        assertEquals("Inception", response.getTitle());
    }

    @Test
    @DisplayName("getMovieById — id inexistente lanza excepción")
    void testGetMovieById_NotFound() {
        when(movieRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> catalogService.getMovieById(1L));
    }

    @Test
    @DisplayName("getMoviesByIds — optimización de lista vacía")
    void testGetMoviesByIds_EmptyList() {
        List<MovieResponse> result = catalogService.getMoviesByIds(List.of());
        assertTrue(result.isEmpty());
        verify(movieRepository, never()).findByIdIn(any());
    }

    @Test
    @DisplayName("createMovie — Upsert de géneros")
    void testCreateMovie_NewGenre() {
        MovieRequest request = new MovieRequest();
        request.setTitle("Dune");
        request.setGenres(List.of("Sci-Fi"));

        Genre newGenre = new Genre();
        newGenre.setName("Sci-Fi");

        when(genreRepository.findByName("Sci-Fi")).thenReturn(Optional.empty());
        when(genreRepository.save(any(Genre.class))).thenReturn(newGenre);
        when(movieRepository.save(any(Movie.class))).thenReturn(mockMovie);

        catalogService.createMovie(request);
        verify(genreRepository, times(1)).save(any(Genre.class));
    }

    @Test
    @DisplayName("deleteMovie — borrado en cascada y evento asíncrono")
    void testDeleteMovie_Success() {
        when(movieRepository.findById(99L)).thenReturn(Optional.of(mockMovie));
        catalogService.deleteMovie(99L);
        verify(reviewRepository, times(1)).deleteByMediaIdAndMediaType(99L, "movie");
        verify(movieRepository, times(1)).deleteById(99L);
        verify(rabbitTemplate, times(1)).convertAndSend(eq("vodeo.exchange"), eq("media.deleted.key"),
                any(MediaDeletedEvent.class));
    }

    @Test
    @DisplayName("getNextEpisode — cálculo exacto del episodio siguiente en la temporada")
    void testGetNextEpisode_InSameSeason() {
        Season mockSeason = new Season();
        mockSeason.setId(10L);

        Episode currentEp = new Episode();
        currentEp.setId(1L);
        currentEp.setEpisodeNumber(1);
        currentEp.setSeason(mockSeason);

        Episode nextEp = new Episode();
        nextEp.setId(2L);
        nextEp.setEpisodeNumber(2);
        nextEp.setSeason(mockSeason);

        when(episodeRepository.findById(1L)).thenReturn(Optional.of(currentEp));
        when(episodeRepository.findFirstBySeasonIdAndEpisodeNumberGreaterThanOrderByEpisodeNumberAsc(10L, 1))
                .thenReturn(Optional.of(nextEp));

        EpisodeResponse result = catalogService.getNextEpisode(1L);
        assertEquals(2L, result.getId());
    }
}