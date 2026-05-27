package com.tfg.streaming.streaming_service.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourceRegion;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRange;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Service
public class StreamingService {

    @Value("${videos.storage-path}")
    private String storagePath;

    private static final long CHUNK_SIZE = 1000000L;

    public ResourceRegion getVideoRegion(String filename, HttpHeaders headers) throws IOException {
        Path filePath = Paths.get(storagePath, filename);
        if (!Files.exists(filePath)) {
            throw new IOException("Archivo de vídeo no encontrado en el servidor");
        }

        Resource video = new FileSystemResource(filePath);
        long contentLength = video.contentLength();

        List<HttpRange> ranges = headers.getRange();
        if (ranges.isEmpty()) {
            long rangeLength = Math.min(CHUNK_SIZE, contentLength);
            return new ResourceRegion(video, 0, rangeLength);
        }

        HttpRange range = ranges.get(0);
        long start = range.getRangeStart(contentLength);
        long end = range.getRangeEnd(contentLength);
        long rangeLength = Math.min(CHUNK_SIZE, end - start + 1);

        return new ResourceRegion(video, start, rangeLength);
    }
}