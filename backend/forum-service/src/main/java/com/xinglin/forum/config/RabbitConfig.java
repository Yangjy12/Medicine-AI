package com.xinglin.forum.config;

import org.springframework.amqp.core.TopicExchange;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitConfig {
    public static final String FORUM_EXCHANGE = "xinglin.forum.exchange";
    public static final String POST_CREATED_ROUTING_KEY = "forum.points.post-created";
    public static final String COMMENT_CREATED_ROUTING_KEY = "forum.points.comment-created";

    @Bean
    public TopicExchange forumExchange() {
        return new TopicExchange(FORUM_EXCHANGE, true, false);
    }
}
