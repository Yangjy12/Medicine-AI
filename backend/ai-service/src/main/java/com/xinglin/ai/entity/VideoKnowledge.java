package com.xinglin.ai.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video")
public class VideoKnowledge {
    @Id
    private Long id;
    private String title;
    @Column(columnDefinition = "TEXT")
    private String description;
    private Long categoryId;
    private String lecturer;
    private String coverUrl;
    private String videoUrl;
    private Integer duration;
    private String tags;
    private Long playCount;
    private Long likeCount;
    private Long collectCount;
    private String status;
    private LocalDateTime publishTime;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
    public String getTags() { return tags; }
    public void setTags(String tags) { this.tags = tags; }
    public Long getPlayCount() { return playCount; }
    public void setPlayCount(Long playCount) { this.playCount = playCount; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public Long getCollectCount() { return collectCount; }
    public void setCollectCount(Long collectCount) { this.collectCount = collectCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
}
