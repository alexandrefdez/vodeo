package com.tfg.streaming.users_service.service;

import com.tfg.streaming.users_service.dto.MediaDeletedEvent;
import com.tfg.streaming.users_service.repository.PlaybackHistoryRepository;
import com.tfg.streaming.users_service.repository.WatchlistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MediaCleanupListener {

    private final WatchlistRepository watchlistRepository;
    private final PlaybackHistoryRepository historyRepository;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "media.deleted.queue", durable = "true"), exchange = @Exchange(value = "vodeo.exchange", type = "topic"), key = "media.deleted.key"))
    @Transactional
    public void handleMediaDeleted(MediaDeletedEvent event) {
        System.out.println("[RabbitMQ] Borrando de BD Usuarios la pelicula: " + event.getMediaId());
        watchlistRepository.deleteByMediaTypeAndMediaId(event.getMediaType(), event.getMediaId());
        historyRepository.deleteByMediaTypeAndMediaId(event.getMediaType(), event.getMediaId());
    }
}