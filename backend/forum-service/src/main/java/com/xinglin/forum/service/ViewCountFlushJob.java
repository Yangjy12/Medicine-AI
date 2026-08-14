package com.xinglin.forum.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class ViewCountFlushJob {
    private static final Logger log = LoggerFactory.getLogger(ViewCountFlushJob.class);
    private final ForumService forumService;

    public ViewCountFlushJob(ForumService forumService) {
        this.forumService = forumService;
    }

    @Scheduled(fixedDelayString = "${xinglin.forum.view-flush-fixed-delay-ms:10000}")
    public void flush() {
        try {
            forumService.flushViewCounts();
        } catch (Exception ex) {
            log.error("forum view count flush job failed", ex);
        }
    }
}
