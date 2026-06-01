package com.tfg.streaming.users_service.controller;

import com.tfg.streaming.users_service.service.WatchlistService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/watchlist")
@RequiredArgsConstructor
public class WatchlistController {

    private final WatchlistService watchlistService;

    @PostMapping("/{movieId}")
    public ResponseEntity<Void> toggleWatchlist(@PathVariable Long movieId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        watchlistService.toggleWatchlist(email, movieId);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<Long>> getWatchlist(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(watchlistService.getUserWatchlist(email, page, size));
    }
}