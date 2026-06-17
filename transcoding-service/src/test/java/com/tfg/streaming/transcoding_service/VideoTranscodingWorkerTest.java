package com.tfg.streaming.transcoding_service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.tfg.streaming.transcoding_service.service.VideoTranscodingWorker;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoTranscodingWorker — Tests Unitarios")
class VideoTranscodingWorkerTest {

    @InjectMocks
    private VideoTranscodingWorker worker;

    @Test
    @DisplayName("buildAbsCommand — asegura la construcción del comando HLS Multi-bitrate")
    @SuppressWarnings("unchecked")
    void testBuildAbsCommand_Logic() {
        List<String> command = (List<String>) ReflectionTestUtils.invokeMethod(
                worker, "buildAbsCommand", "/tmp/source.mp4", "seg%03d.ts", "playlist.m3u8");

        assertNotNull(command);
        String fullCommand = String.join(" ", command);

        assertTrue(fullCommand.contains("ffmpeg"));
        assertTrue(fullCommand.contains("-i /tmp/source.mp4"));
        assertTrue(fullCommand.contains("[0:v]split=3[v1][v2][v3]"));

        // Verifica resoluciones y bitrates ABS
        assertTrue(fullCommand.contains("scale=1920:1080"));
        assertTrue(fullCommand.contains("-b:v:0 5000k"));
        assertTrue(fullCommand.contains("scale=1280:720"));
        assertTrue(fullCommand.contains("-b:v:1 2800k"));

        // Verifica playlist HLS
        assertTrue(fullCommand.contains("-master_pl_name master.m3u8"));
    }
}