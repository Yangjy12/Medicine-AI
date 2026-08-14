package com.xinglin.forum.dto;

public class AdminPostStatusRequest {
    private String status;
    private Boolean topFlag;
    private Boolean essenceFlag;

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public Boolean getTopFlag() { return topFlag; }
    public void setTopFlag(Boolean topFlag) { this.topFlag = topFlag; }
    public Boolean getEssenceFlag() { return essenceFlag; }
    public void setEssenceFlag(Boolean essenceFlag) { this.essenceFlag = essenceFlag; }
}
