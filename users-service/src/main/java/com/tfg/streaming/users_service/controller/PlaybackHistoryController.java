package com.tfg.streaming.users_service.controller;

import com.tfg.streaming.users_service.dto.PlaybackHistoryDto;
import com.tfg.streaming.users_service.service.PlaybackHistoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users/history")
@RequiredArgsConstructor
public class PlaybackHistoryController {

    private final PlaybackHistoryService historyService;

    @PostMapping
    public ResponseEntity<Void> saveProgress(@RequestBody PlaybackHistoryDto dto) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        historyService.saveProgress(email, dto);
        return ResponseEntity.ok().build();
    }

    @GetMapping
    public ResponseEntity<Page<PlaybackHistoryDto>> getHistory(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(historyService.getUserHistory(email, page, size));
    }

    @GetMapping("/progress/{mediaType}/{mediaId}")
    public ResponseEntity<Integer> getProgress(@PathVariable String mediaType, @PathVariable Long mediaId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return ResponseEntity.ok(historyService.getProgress(email, mediaId, mediaType));
    }
}