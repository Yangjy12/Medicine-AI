package com.xinglin.video.dto;

public class PlayRequest {
    private Integer playedSecond = 0;
    private Integer duration = 0;

    public Integer getPlayedSecond() { return playedSecond; }
    public void setPlayedSecond(Integer playedSecond) { this.playedSecond = playedSecond; }
    public Integer getDuration() { return duration; }
    public void setDuration(Integer duration) { this.duration = duration; }
}
