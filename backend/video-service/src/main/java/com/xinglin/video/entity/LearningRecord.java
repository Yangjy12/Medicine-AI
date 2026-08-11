package com.xinglin.video.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "learning_record", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}))
public class LearningRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "video_id")
    private Long videoId;
    private Integer currentSecond = 0;
    private Integer duration = 0;
    private Integer progressPercent = 0;
    private Boolean finished = false;
    private LocalDateTime firstFinishTime;
    private LocalDateTime lastLearnTime = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public Integer getCurrentSecond() { return currentSecond; }
    public void setCurrentSecond(Integer currentSecond) { this.currentSecond = currentSecond; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }
    public LocalDateTime getFirstFinishTime() { return firstFinishTime; }
    public void setFirstFinishTime(LocalDateTime firstFinishTime) { this.firstFinishTime = firstFinishTime; }
    public LocalDateTime getLastLearnTime() { return lastLearnTime; }
    public void setLastLearnTime(LocalDateTime lastLearnTime) { this.lastLearnTime = lastLearnTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
