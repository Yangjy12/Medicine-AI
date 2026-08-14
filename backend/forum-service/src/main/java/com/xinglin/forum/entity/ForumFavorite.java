package com.xinglin.forum.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "forum_favorite", uniqueConstraints = {
        @UniqueConstraint(name = "uk_forum_favorite_user_post", columnNames = {"user_id", "post_id"})
}, indexes = {
        @Index(name = "idx_forum_favorite_user_time", columnList = "user_id,created_at"),
        @Index(name = "idx_forum_favorite_post", columnList = "post_id")
})
public class ForumFavorite {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userId;
    private Long postId;
    private LocalDateTime createdAt = LocalDateTime.now();

    public ForumFavorite() {
    }

    public ForumFavorite(Long userId, Long postId) {
        this.userId = userId;
        this.postId = postId;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
