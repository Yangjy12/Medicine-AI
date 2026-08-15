package com.xinglin.user.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String VIDEO_EXCHANGE = "xinglin.video.exchange";
    public static final String VIDEO_LEARNING_QUEUE = "xinglin.user.video-learning.queue";
    public static final String VIDEO_LEARNING_ROUTING_KEY = "video.learning.finished";
    public static final String FORUM_EXCHANGE = "xinglin.forum.exchange";
    public static final String FORUM_POINTS_QUEUE = "xinglin.user.forum-points.queue";
    public static final String FORUM_POINTS_ROUTING_KEY = "forum.points.*";

    @Bean
    public TopicExchange videoExchange() {
        return new TopicExchange(VIDEO_EXCHANGE, true, false);
    }

    @Bean
    public Queue videoLearningQueue() {
        return new Queue(VIDEO_LEARNING_QUEUE, true);
    }

    @Bean
    public Binding videoLearningBinding(@Qualifier("videoLearningQueue") Queue videoLearningQueue,
                                        @Qualifier("videoExchange") TopicExchange videoExchange) {
        return BindingBuilder.bind(videoLearningQueue).to(videoExchange).with(VIDEO_LEARNING_ROUTING_KEY);
    }

    @Bean
    public TopicExchange forumExchange() {
        return new TopicExchange(FORUM_EXCHANGE, true, false);
    }

    @Bean
    public Queue forumPointsQueue() {
        return new Queue(FORUM_POINTS_QUEUE, true);
    }

    @Bean
    public Binding forumPointsBinding(@Qualifier("forumPointsQueue") Queue forumPointsQueue,
                                      @Qualifier("forumExchange") TopicExchange forumExchange) {
        return BindingBuilder.bind(forumPointsQueue).to(forumExchange).with(FORUM_POINTS_ROUTING_KEY);
    }
}
