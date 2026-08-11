package com.xinglin.video.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "video_favorite", uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "video_id"}))
public class VideoFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(name = "video_id")
    private Long videoId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public VideoFavorite() {}

    public VideoFavorite(Long userId, Long videoId) {
        this.userId = userId;
        this.videoId = videoId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
