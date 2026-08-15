package com.xinglin.chat.dto;

import javax.validation.constraints.NotEmpty;
import java.util.ArrayList;
import java.util.List;

public class AddMembersRequest {
    @NotEmpty(message = "请选择要添加的成员")
    private List<Long> memberIds = new ArrayList<>();

    public List<Long> getMemberIds() { return memberIds; }
    public void setMemberIds(List<Long> memberIds) { this.memberIds = memberIds; }
}
