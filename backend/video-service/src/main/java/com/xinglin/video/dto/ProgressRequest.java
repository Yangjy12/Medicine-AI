package com.xinglin.video.dto;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class ProgressRequest {
    @NotNull
    @Min(0)
    private Integer currentSecond;

    @NotNull
    @Min(1)
    private Integer duration;

    public Integer getCurrentSecond() { return currentSecond; }
    public void setCurrentSecond(Integer currentSecond) { this.currentSecond = currentSecond; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
