package com.tfg.streaming.users_service.repository;

import com.tfg.streaming.users_service.model.User;
import com.tfg.streaming.users_service.model.Watchlist;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface WatchlistRepository extends JpaRepository<Watchlist, Long> {
    Page<Watchlist> findByUserOrderByIdDesc(User user, Pageable pageable);

    Optional<Watchlist> findByUserAndMediaIdAndMediaType(User user, Long mediaId, String mediaType);
}