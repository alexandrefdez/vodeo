package com.tfg.streaming.catalog_service.model;

import jakarta.persistence.*;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "movies")
@Data
public class Movie {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer year;

    private Integer duration;

    private String videoPath;

    @Column(length = 500)
    private String posterUrl;
    // Nuevos campos para pagina de detalles
    @Column(length = 500)
    private String backdropUrl;

    private Double tmdbRating;

    @Column(length = 50)
    private String contentRating;

    private String director;

    @Column(length = 1000)
    private String castCrew;

    @ManyToMany
    @JoinTable(name = "movie_genres", joinColumns = @JoinColumn(name = "movie_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<Genre> genres;

    @OneToMany(mappedBy = "movie", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtitle> subtitles = new ArrayList<>();
}