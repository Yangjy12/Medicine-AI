package com.xinglin.user.service;

import com.xinglin.user.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class VideoLearningPointsConsumer {
    private static final Logger log = LoggerFactory.getLogger(VideoLearningPointsConsumer.class);
    private static final String VIDEO_FINISHED = "VIDEO_LEARNING_FINISHED";
    private final PointsService pointsService;
    private final PointsRuleService pointsRuleService;

    public VideoLearningPointsConsumer(PointsService pointsService, PointsRuleService pointsRuleService) {
        this.pointsService = pointsService;
        this.pointsRuleService = pointsRuleService;
    }

    @RabbitListener(queues = RabbitConfig.VIDEO_LEARNING_QUEUE)
    public void consume(Map<String, Object> event) {
        String eventId = value(event, "eventId");
        String eventType = value(event, "eventType");
        if (!VIDEO_FINISHED.equals(eventType)) {
            log.warn("mq event ignored queue={} eventId={} eventType={} payload={}",
                    RabbitConfig.VIDEO_LEARNING_QUEUE, eventId, eventType, event);
            return;
        }
        Long userId = parsePositiveLong(value(event, "userId"), "userId", eventId);
        if (userId == null) {
            return;
        }
        String videoId = value(event, "videoId");
        if (!StringUtils.hasText(videoId)) {
            log.warn("mq event ignored missing videoId queue={} eventId={} payload={}",
                    RabbitConfig.VIDEO_LEARNING_QUEUE, eventId, event);
            return;
        }
        log.info("mq consume start queue={} eventId={} eventType={} userId={} videoId={}",
                RabbitConfig.VIDEO_LEARNING_QUEUE, eventId, eventType, userId, videoId);
        int points = pointsRuleService.requireEnabledRule("VIDEO_FINISH").getPoints();
        pointsService.addPoints(userId, "VIDEO_FINISH", videoId, points, "完成视频学习");
        log.info("mq consume success queue={} eventId={}", RabbitConfig.VIDEO_LEARNING_QUEUE, eventId);
    }

    private String value(Map<String, Object> event, String key) {
        Object value = event == null ? null : event.get(key);
        return value == null ? null : String.valueOf(value);
    }

    private Long parsePositiveLong(String value, String field, String eventId) {
        try {
            Long parsed = Long.valueOf(value);
            if (parsed > 0) {
                return parsed;
            }
        } catch (Exception ignored) {
            // Log a uniform invalid-field message below.
        }
        log.warn("mq event ignored invalid {} queue={} eventId={} value={}",
                field, RabbitConfig.VIDEO_LEARNING_QUEUE, eventId, value);
        return null;
    }
}
