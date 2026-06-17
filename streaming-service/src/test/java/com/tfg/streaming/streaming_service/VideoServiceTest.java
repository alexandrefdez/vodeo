package com.tfg.streaming.streaming_service;

import com.tfg.streaming.streaming_service.config.RabbitMQConfig;
import com.tfg.streaming.streaming_service.dto.VideoEvent;
import com.tfg.streaming.streaming_service.service.VideoService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("VideoService — Tests Unitarios")
class VideoServiceTest {

    @Mock
    private RabbitTemplate rabbitTemplate;
    @InjectMocks
    private VideoService videoService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(videoService, "storagePath", System.getProperty("java.io.tmpdir") + "/");
    }

    @Test
    @DisplayName("uploadVideo — archivo MP4 notifica a RabbitMQ")
    void testUploadVideo_Mp4_PublishesEvent() throws IOException {
        MockMultipartFile mockFile = new MockMultipartFile("file", "test.mp4", "video/mp4", "dummy".getBytes());
        String generatedFilename = videoService.uploadVideo(mockFile);

        assertTrue(generatedFilename.endsWith(".mp4"));
        ArgumentCaptor<VideoEvent> eventCaptor = ArgumentCaptor.forClass(VideoEvent.class);

        verify(rabbitTemplate, times(1)).convertAndSend(
                eq(RabbitMQConfig.VIDEO_EXCHANGE),
                eq(RabbitMQConfig.VIDEO_ROUTING_KEY),
                eventCaptor.capture());
        assertEquals(generatedFilename, eventCaptor.getValue().getFilename());
    }

    @Test
    @DisplayName("uploadVideo — subtítulo omite RabbitMQ")
    void testUploadVideo_Subtitle_DoesNotPublishEvent() throws IOException {
        MockMultipartFile mockSubtitle = new MockMultipartFile("file", "sub.vtt", "text/vtt", "dummy".getBytes());
        String generatedFilename = videoService.uploadVideo(mockSubtitle);
        assertTrue(generatedFilename.endsWith(".vtt"));
        verify(rabbitTemplate, never()).convertAndSend(anyString(), anyString(), any(VideoEvent.class));
    }
}