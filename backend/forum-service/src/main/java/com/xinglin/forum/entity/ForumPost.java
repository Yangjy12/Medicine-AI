package com.xinglin.forum.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_post", indexes = {
        @Index(name = "idx_forum_post_board_status_time", columnList = "board_id,status,publish_time"),
        @Index(name = "idx_forum_post_user_time", columnList = "user_id,created_at"),
        @Index(name = "idx_forum_post_hot", columnList = "status,hot_score"),
        @Index(name = "idx_forum_post_publish_time", columnList = "status,publish_time")
})
public class ForumPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long boardId;
    private Long userId;
    @Column(nullable = false, length = 128)
    private String title;
    @Column(length = 512)
    private String summary;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String content;
    @Column(columnDefinition = "MEDIUMTEXT")
    private String contentText;
    private String coverUrl;
    private String coverObjectKey;
    private Long viewCount = 0L;
    private Long likeCount = 0L;
    private Long commentCount = 0L;
    private Long favoriteCount = 0L;
    private Long hotScore = 0L;
    private Boolean topFlag = false;
    private Boolean essenceFlag = false;
    private String status = "PUBLISHED";
    private LocalDateTime publishTime = LocalDateTime.now();
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getContentText() { return contentText; }
    public void setContentText(String contentText) { this.contentText = contentText; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getCoverObjectKey() { return coverObjectKey; }
    public void setCoverObjectKey(String coverObjectKey) { this.coverObjectKey = coverObjectKey; }
    public Long getViewCount() { return viewCount; }
    public void setViewCount(Long viewCount) { this.viewCount = viewCount; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public Long getCommentCount() { return commentCount; }
    public void setCommentCount(Long commentCount) { this.commentCount = commentCount; }
    public Long getFavoriteCount() { return favoriteCount; }
    public void setFavoriteCount(Long favoriteCount) { this.favoriteCount = favoriteCount; }
    public Long getHotScore() { return hotScore; }
    public void setHotScore(Long hotScore) { this.hotScore = hotScore; }
    public Boolean getTopFlag() { return topFlag; }
    public void setTopFlag(Boolean topFlag) { this.topFlag = topFlag; }
    public Boolean getEssenceFlag() { return essenceFlag; }
    public void setEssenceFlag(Boolean essenceFlag) { this.essenceFlag = essenceFlag; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getPublishTime() { return publishTime; }
    public void setPublishTime(LocalDateTime publishTime) { this.publishTime = publishTime; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
