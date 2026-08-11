package com.xinglin.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String VIDEO_EXCHANGE = "xinglin.video.exchange";
    public static final String VIDEO_LEARNING_QUEUE = "xinglin.user.video-learning.queue";
    public static final String VIDEO_LEARNING_ROUTING_KEY = "video.learning.finished";

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange(VIDEO_EXCHANGE, true, false);
    }

    @Bean
    public Queue videoLearningQueue() {
        return new Queue(VIDEO_LEARNING_QUEUE, true);
    }

    @Bean
    public Binding videoLearningBinding(Queue videoLearningQueue, TopicExchange videoExchange) {
        return BindingBuilder.bind(videoLearningQueue).to(videoExchange).with(VIDEO_LEARNING_ROUTING_KEY);
    }
}
