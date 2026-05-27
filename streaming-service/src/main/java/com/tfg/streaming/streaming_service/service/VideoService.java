package com.tfg.streaming.streaming_service.service;

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
public class VideoService {

    @Value("${videos.storage-path}")
    private String storagePath;

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

        return newFilename;
    }
}