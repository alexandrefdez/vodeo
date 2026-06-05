package com.tfg.streaming.catalog_service.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Entity
@Table(name = "subtitles")
@Data
public class Subtitle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String language;

    @Column(nullable = false)
    private String languageCode;

    @Column(nullable = false)
    private String subtitlePath;

    // Relación con Películas (Puede ser nulo si el subtítulo es de un episodio)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "movie_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Movie movie;

    // Relación con Episodios (Puede ser nulo si el subtítulo es de una película)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "episode_id")
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Episode episode;
}