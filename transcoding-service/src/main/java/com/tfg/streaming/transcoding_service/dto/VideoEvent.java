package com.tfg.streaming.transcoding_service.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEvent {
    private String filename;
    private String absolutePath;
}