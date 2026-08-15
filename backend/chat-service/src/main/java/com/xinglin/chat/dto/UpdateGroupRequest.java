package com.xinglin.chat.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class UpdateGroupRequest {
    @NotBlank(message = "群名称不能为空")
    @Size(max = 64, message = "群名称不能超过64字")
    private String title;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
}
