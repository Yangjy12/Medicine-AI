package com.xinglin.forum.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_comment", indexes = {
        @Index(name = "idx_forum_comment_post_root_time", columnList = "post_id,root_id,created_at"),
        @Index(name = "idx_forum_comment_post_parent_time", columnList = "post_id,parent_id,created_at"),
        @Index(name = "idx_forum_comment_user_time", columnList = "user_id,created_at")
})
public class ForumComment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long postId;
    private Long userId;
    private Long parentId = 0L;
    private Long rootId = 0L;
    private Long replyToUserId;
    @Column(nullable = false, length = 2000)
    private String content;
    private Long likeCount = 0L;
    private String status = "NORMAL";
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
    public Long getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Long replyToUserId) { this.replyToUserId = replyToUserId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
