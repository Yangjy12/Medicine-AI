package com.xinglin.forum.vo;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class CommentVO {
    private Long id;
    private Long postId;
    private Long userId;
    private String authorName;
    private String authorAvatar;
    private Long parentId;
    private Long rootId;
    private Long replyToUserId;
    private String replyToUserName;
    private String replyToUserAvatar;
    private String content;
    private Long likeCount;
    private Boolean liked = false;
    private LocalDateTime createdAt;
    private List<CommentVO> replies = new ArrayList<>();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }
    public String getAuthorAvatar() { return authorAvatar; }
    public void setAuthorAvatar(String authorAvatar) { this.authorAvatar = authorAvatar; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public Long getRootId() { return rootId; }
    public void setRootId(Long rootId) { this.rootId = rootId; }
    public Long getReplyToUserId() { return replyToUserId; }
    public void setReplyToUserId(Long replyToUserId) { this.replyToUserId = replyToUserId; }
    public String getReplyToUserName() { return replyToUserName; }
    public void setReplyToUserName(String replyToUserName) { this.replyToUserName = replyToUserName; }
    public String getReplyToUserAvatar() { return replyToUserAvatar; }
    public void setReplyToUserAvatar(String replyToUserAvatar) { this.replyToUserAvatar = replyToUserAvatar; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Long getLikeCount() { return likeCount; }
    public void setLikeCount(Long likeCount) { this.likeCount = likeCount; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public List<CommentVO> getReplies() { return replies; }
    public void setReplies(List<CommentVO> replies) { this.replies = replies; }
}
