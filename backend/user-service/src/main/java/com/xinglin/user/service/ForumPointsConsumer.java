package com.xinglin.user.service;

import com.xinglin.user.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;

@Component
public class ForumPointsConsumer {
    private static final Logger log = LoggerFactory.getLogger(ForumPointsConsumer.class);
    private static final String POST_CREATED = "POST_CREATED";
    private static final String COMMENT_CREATED = "COMMENT_CREATED";

    private final PointsService pointsService;
    private final PointsRuleService pointsRuleService;

    public ForumPointsConsumer(PointsService pointsService, PointsRuleService pointsRuleService) {
        this.pointsService = pointsService;
        this.pointsRuleService = pointsRuleService;
    }

    @RabbitListener(queues = RabbitConfig.FORUM_POINTS_QUEUE)
    public void consume(Map<String, Object> event) {
        String eventId = value(event, "eventId");
        String eventType = value(event, "eventType");
        Long userId = parsePositiveLong(value(event, "userId"), "userId", eventId);
        if (userId == null) {
            return;
        }
        if (POST_CREATED.equals(eventType)) {
            award(userId, "POST_CREATE", value(event, "postId"), "发布帖子", eventId, eventType, event);
            return;
        }
        if (COMMENT_CREATED.equals(eventType)) {
            award(userId, "COMMENT_CREATE", value(event, "commentId"), "发表评论", eventId, eventType, event);
            return;
        }
        log.warn("mq event ignored queue={} eventId={} eventType={} payload={}",
                RabbitConfig.FORUM_POINTS_QUEUE, eventId, eventType, event);
    }

    private void award(Long userId,
                       String bizType,
                       String bizId,
                       String description,
                       String eventId,
                       String eventType,
                       Map<String, Object> event) {
        if (!StringUtils.hasText(bizId)) {
            log.warn("mq event ignored missing bizId queue={} eventId={} eventType={} payload={}",
                    RabbitConfig.FORUM_POINTS_QUEUE, eventId, eventType, event);
            return;
        }
        log.info("mq consume start queue={} eventId={} eventType={} userId={} bizType={} bizId={}",
                RabbitConfig.FORUM_POINTS_QUEUE, eventId, eventType, userId, bizType, bizId);
        int points = pointsRuleService.requireEnabledRule(bizType).getPoints();
        pointsService.addPoints(userId, bizType, bizId, points, description);
        log.info("mq consume success queue={} eventId={} eventType={} userId={} bizType={} bizId={}",
                RabbitConfig.FORUM_POINTS_QUEUE, eventId, eventType, userId, bizType, bizId);
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
                field, RabbitConfig.FORUM_POINTS_QUEUE, eventId, value);
        return null;
    }
}
