package com.xinglin.video.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class VideoDetailVO extends VideoCardVO {
    private String description;
    private String videoUrl;
    private Boolean liked = false;
    private Boolean collected = false;
    private ProgressVO progress = new ProgressVO(0, 0, false);
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private List<VideoCardVO> relatedVideos = new ArrayList<>();

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
    public Boolean getCollected() { return collected; }
    public void setCollected(Boolean collected) { this.collected = collected; }
    public ProgressVO getProgress() { return progress; }
    public void setProgress(ProgressVO progress) { this.progress = progress; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    public List<VideoCardVO> getRelatedVideos() { return relatedVideos; }
    public void setRelatedVideos(List<VideoCardVO> relatedVideos) { this.relatedVideos = relatedVideos; }
}
