package com.tfg.streaming.catalog_service.service;

import com.tfg.streaming.catalog_service.dto.UserDeletedEvent;
import com.tfg.streaming.catalog_service.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserCleanupListener {

    private final ReviewRepository reviewRepository;

    @RabbitListener(bindings = @QueueBinding(value = @Queue(value = "user.deleted.queue", durable = "true"), exchange = @Exchange(value = "vodeo.exchange", type = "topic"), key = "user.deleted.key"))
    @Transactional
    public void handleUserDeleted(UserDeletedEvent event) {
        System.out.println("[RabbitMQ] Anonimizando reseñas de: " + event.getUsername());
        reviewRepository.anonymizeReviewsByUsername(event.getUsername());
    }
}