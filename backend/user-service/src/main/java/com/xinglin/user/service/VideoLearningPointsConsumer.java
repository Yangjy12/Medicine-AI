package com.xinglin.user.service;

import com.xinglin.user.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class VideoLearningPointsConsumer {
    private static final Logger log = LoggerFactory.getLogger(VideoLearningPointsConsumer.class);
    private final PointsService pointsService;

    public VideoLearningPointsConsumer(PointsService pointsService) {
        this.pointsService = pointsService;
    }

    @RabbitListener(queues = RabbitConfig.VIDEO_LEARNING_QUEUE)
    public void consume(Map<String, Object> event) {
        String eventId = String.valueOf(event.get("eventId"));
        Long userId = Long.valueOf(String.valueOf(event.get("userId")));
        String videoId = String.valueOf(event.get("videoId"));
        log.info("mq consume start queue={} eventId={} eventType={} userId={} videoId={}",
                RabbitConfig.VIDEO_LEARNING_QUEUE, eventId, event.get("eventType"), userId, videoId);
        pointsService.addPoints(userId, "VIDEO_FINISH", videoId, 10, "完成视频学习");
        log.info("mq consume success queue={} eventId={}", RabbitConfig.VIDEO_LEARNING_QUEUE, eventId);
    }
}
