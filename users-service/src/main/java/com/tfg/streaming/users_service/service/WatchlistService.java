package com.tfg.streaming.users_service.service;

import com.tfg.streaming.users_service.dto.WatchlistItemDto;
import com.tfg.streaming.users_service.exception.ResourceNotFoundException;
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

    public void toggleWatchlist(String email, Long mediaId, String mediaType) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Optional<Watchlist> existing = watchlistRepository.findByUserAndMediaIdAndMediaType(user, mediaId, mediaType);

        if (existing.isPresent()) {
            watchlistRepository.delete(existing.get());
        } else {
            Watchlist watchlist = new Watchlist();
            watchlist.setUser(user);
            watchlist.setMediaId(mediaId);
            watchlist.setMediaType(mediaType);
            watchlistRepository.save(watchlist);
        }
    }

    public Page<WatchlistItemDto> getUserWatchlist(String email, int page, int size) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return watchlistRepository.findByUserOrderByIdDesc(user, PageRequest.of(page, size))
                .map(w -> new WatchlistItemDto(w.getMediaId(), w.getMediaType()));
    }
}