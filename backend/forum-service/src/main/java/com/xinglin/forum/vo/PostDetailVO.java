package com.xinglin.forum.vo;

public class PostDetailVO extends PostCardVO {
    private String content;
    private Boolean liked = false;
    private Boolean favorited = false;

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Boolean getLiked() { return liked; }
    public void setLiked(Boolean liked) { this.liked = liked; }
    public Boolean getFavorited() { return favorited; }
    public void setFavorited(Boolean favorited) { this.favorited = favorited; }
}
