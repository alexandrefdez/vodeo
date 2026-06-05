package com.tfg.streaming.streaming_service.controller;

import com.tfg.streaming.streaming_service.service.StreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import java.io.IOException;

@RestController
@RequestMapping("/api/streaming")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    @GetMapping(value = "/play/{filename}", produces = "video/mp4")
    public ResponseEntity<ResourceRegion> streamVideo(
            @PathVariable String filename,
            @RequestHeader HttpHeaders headers) {
        try {
            ResourceRegion region = streamingService.getVideoRegion(filename, headers);
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT)
                    .body(region);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @DeleteMapping("/video")
    public ResponseEntity<Void> deleteVideo(@RequestParam String videoPath) {
        try {
            streamingService.deleteVideo(videoPath);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping(value = "/subtitles/{filename}", produces = "text/vtt")
    public ResponseEntity<Resource> getSubtitle(@PathVariable String filename) {
        try {
            Resource file = streamingService.getFileAsResource(filename);
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE, "text/vtt")
                    .body(file);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}