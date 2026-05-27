package com.tfg.streaming.streaming_service.controller;

import com.tfg.streaming.streaming_service.service.StreamingService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

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
            return ResponseEntity.status(HttpStatus.PARTIAL_CONTENT) //Codigo HTTP 206 indica que se esta enviado el trozo del video solicitado
                    .body(region); //Metemos los bytes calculados en el body de la respuesta
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build(); //Vacio porque lo recibe el reproductor de video del navegador
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build(); //Vacio porque lo recibe el reproductor de video del navegador
        }
    }
}