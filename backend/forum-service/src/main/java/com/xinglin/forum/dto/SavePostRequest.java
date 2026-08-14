package com.xinglin.forum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SavePostRequest {
    @NotNull(message = "请选择板块")
    private Long boardId;
    @NotBlank(message = "标题不能为空")
    @Size(min = 5, max = 128, message = "标题长度必须在5到128字之间")
    private String title;
    @NotBlank(message = "正文不能为空")
    @Size(min = 10, max = 20000, message = "正文长度必须在10到20000字之间")
    private String content;
    private String coverUrl;
    private String coverObjectKey;

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public String getCoverObjectKey() { return coverObjectKey; }
    public void setCoverObjectKey(String coverObjectKey) { this.coverObjectKey = coverObjectKey; }
}
