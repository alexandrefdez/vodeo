package com.tfg.streaming.catalog_service.model;

import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "episodes")
@Data
public class Episode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer episodeNumber;

    @Column(nullable = false)
    private String title;

    @Column(length = 2000)
    private String description;

    private Integer duration;

    private String videoPath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "season_id", nullable = false)
    private Season season;

    @OneToMany(mappedBy = "episode", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Subtitle> subtitles = new ArrayList<>();
}