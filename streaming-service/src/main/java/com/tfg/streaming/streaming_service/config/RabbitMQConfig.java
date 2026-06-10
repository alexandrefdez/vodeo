package com.tfg.streaming.streaming_service.config;

import org.springframework.amqp.core.*;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.amqp.support.converter.MessageConverter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String VIDEO_QUEUE = "video_transcoding_queue";
    public static final String VIDEO_EXCHANGE = "video_exchange";
    public static final String VIDEO_ROUTING_KEY = "video_routing_key";

    @Bean
    public Queue videoQueue() {
        return new Queue(VIDEO_QUEUE, true); // true hace que la cola sobrevive si RabbitMQ se reinicia
    }

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange(VIDEO_EXCHANGE);
    }

    @Bean
    public Binding binding(Queue videoQueue, TopicExchange videoExchange) {
        return BindingBuilder.bind(videoQueue).to(videoExchange).with(VIDEO_ROUTING_KEY);
    }

    @Bean
    public MessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter(); // Para que el mensaje viaje como JSON
    }
}