package com.xinglin.forum.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class SaveCommentRequest {
    private Long parentId = 0L;
    @NotBlank(message = "评论内容不能为空")
    @Size(max = 2000, message = "评论内容不能超过2000字")
    private String content;

    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
}
