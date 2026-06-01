package com.tfg.streaming.users_service.service;

import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.model.Watchlist;
import com.tfg.streaming.users_service.repository.UserRepository;
import com.tfg.streaming.users_service.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WatchlistService {

    private final WatchlistRepository watchlistRepository;
    private final UserRepository userRepository;

    public void toggleWatchlist(String email, Long movieId) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        Optional<Watchlist> existing = watchlistRepository.findByUserAndMovieId(user, movieId);

        if (existing.isPresent()) {
            watchlistRepository.delete(existing.get());
        } else {
            Watchlist watchlist = new Watchlist();
            watchlist.setUser(user);
            watchlist.setMovieId(movieId);
            watchlistRepository.save(watchlist);
        }
    }

    public Page<Long> getUserWatchlist(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        return watchlistRepository.findByUserOrderByIdDesc(user, PageRequest.of(page, size))
                .map(Watchlist::getMovieId);
    }
}