package com.tfg.streaming.streaming_service.service;

import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FileCleanupListener {

    private final StreamingService streamingService;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "file.deleted.queue", durable = "true"), exchange = @Exchange(value = "vodeo.exchange", type = "topic"), key = "file.delete.key"))
    public void handleFileDeleted(String filePath) {
        System.out.println("[RabbitMQ] Borrando del disco duro el archivo: " + filePath);
        streamingService.deleteVideo(filePath);
    }
}