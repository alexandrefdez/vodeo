package com.tfg.streaming.streaming_service.controller;

import com.tfg.streaming.streaming_service.service.StreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.core.io.Resource;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import java.nio.file.Path;
import java.nio.file.Paths;

import java.io.IOException;

@RestController
@RequestMapping("/api/streaming")
@RequiredArgsConstructor
public class StreamingController {

    private final StreamingService streamingService;

    @Value("${videos.storage-path}")
    private String storagePath;

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

    @GetMapping("/hls/{folder}/{filename}")
    public ResponseEntity<Resource> serveHlsFile(
            @PathVariable String folder,
            @PathVariable String filename) {
        try {
            if (folder.contains("..") || filename.contains("..")) {
                return ResponseEntity.badRequest().build();
            }

            Path filePath = Paths.get(storagePath, folder, filename);
            Resource resource = new FileSystemResource(filePath);

            if (!resource.exists()) {
                return ResponseEntity.notFound().build();
            }

            String contentType = "application/octet-stream";
            if (filename.endsWith(".m3u8")) {
                contentType = "application/vnd.apple.mpegurl";
            } else if (filename.endsWith(".ts")) {
                contentType = "video/mp2t";
            }

            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(resource);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}