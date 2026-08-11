package com.xinglin.video.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String VIDEO_EXCHANGE = "xinglin.video.exchange";
    public static final String LEARNING_QUEUE = "xinglin.video.learning.queue";
    public static final String LEARNING_ROUTING_KEY = "video.learning.finished";

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange(VIDEO_EXCHANGE, true, false);
    }

    @Bean
    public Queue learningQueue() {
        return new Queue(LEARNING_QUEUE, true);
    }

    @Bean
    public Binding learningBinding(Queue learningQueue, TopicExchange videoExchange) {
        return BindingBuilder.bind(learningQueue).to(videoExchange).with(LEARNING_ROUTING_KEY);
    }
}
