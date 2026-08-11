package com.xinglin.video.vo;

public class ProgressVO {
    private Integer currentSecond;
    private Integer progressPercent;
    private Boolean finished;

    public ProgressVO() {}

    public ProgressVO(Integer currentSecond, Integer progressPercent, Boolean finished) {
        this.currentSecond = currentSecond;
        this.progressPercent = progressPercent;
        this.finished = finished;
    }

    public Integer getCurrentSecond() { return currentSecond; }
    public void setCurrentSecond(Integer currentSecond) { this.currentSecond = currentSecond; }
    public Integer getProgressPercent() { return progressPercent; }
    public void setProgressPercent(Integer progressPercent) { this.progressPercent = progressPercent; }
    public Boolean getFinished() { return finished; }
    public void setFinished(Boolean finished) { this.finished = finished; }
}
