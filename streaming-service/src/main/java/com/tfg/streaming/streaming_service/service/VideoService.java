package com.tfg.streaming.streaming_service.service;

import com.tfg.streaming.streaming_service.config.RabbitMQConfig;
import com.tfg.streaming.streaming_service.dto.VideoEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class VideoService {

    @Value("${videos.storage-path}")
    private String storagePath;

    // INYECTAR EL MOTOR DE RABBITMQ
    private final RabbitTemplate rabbitTemplate;

    public String uploadVideo(MultipartFile file) throws IOException {
        File directory = new File(storagePath);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".")
                ? originalFilename.substring(originalFilename.lastIndexOf("."))
                : ".mp4";

        String newFilename = UUID.randomUUID().toString() + extension;
        Path filePath = Paths.get(storagePath, newFilename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Si no es un subtitulo entonces es un video por lo que le mandamos el recado a
        // la cola
        if (!extension.equalsIgnoreCase(".vtt") && !extension.equalsIgnoreCase(".srt")) {
            VideoEvent event = new VideoEvent(newFilename, newFilename);
            rabbitTemplate.convertAndSend(
                    RabbitMQConfig.VIDEO_EXCHANGE,
                    RabbitMQConfig.VIDEO_ROUTING_KEY,
                    event);
        }

        return newFilename;
    }
}