package com.tfg.streaming.catalog_service.model;

import jakarta.persistence.*;
import lombok.Data;
import java.util.List;

@Entity
@Table(name = "series")
@Data
public class Series {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer startYear;

    @Column(length = 500)
    private String posterUrl;

    @Column(length = 500)
    private String backdropUrl;

    private Double tmdbRating;

    @Column(length = 50)
    private String contentRating;

    private String director;

    @Column(length = 1000)
    private String castCrew;

    @ManyToMany
    @JoinTable(name = "series_genres", joinColumns = @JoinColumn(name = "series_id"), inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private List<Genre> genres;

    @OneToMany(mappedBy = "series", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Season> seasons;
}