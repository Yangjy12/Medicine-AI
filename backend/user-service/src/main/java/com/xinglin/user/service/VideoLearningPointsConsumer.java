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
    private final PointsRuleService pointsRuleService;

    public VideoLearningPointsConsumer(PointsService pointsService, PointsRuleService pointsRuleService) {
        this.pointsService = pointsService;
        this.pointsRuleService = pointsRuleService;
    }

    @RabbitListener(queues = RabbitConfig.VIDEO_LEARNING_QUEUE)
    public void consume(Map<String, Object> event) {
        String eventId = String.valueOf(event.get("eventId"));
        Long userId = Long.valueOf(String.valueOf(event.get("userId")));
        String videoId = String.valueOf(event.get("videoId"));
        log.info("mq consume start queue={} eventId={} eventType={} userId={} videoId={}",
                RabbitConfig.VIDEO_LEARNING_QUEUE, eventId, event.get("eventType"), userId, videoId);
        int points = pointsRuleService.requireEnabledRule("VIDEO_FINISH").getPoints();
        pointsService.addPoints(userId, "VIDEO_FINISH", videoId, points, "完成视频学习");
        log.info("mq consume success queue={} eventId={}", RabbitConfig.VIDEO_LEARNING_QUEUE, eventId);
    }
}
