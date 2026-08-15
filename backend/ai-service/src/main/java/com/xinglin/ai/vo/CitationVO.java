package com.xinglin.ai.vo;

public class CitationVO {
    private Long videoId;
    private String title;
    private String lecturer;
    private String summary;
    private String videoUrl;
    private String coverUrl;
    private Long playCount;

    public Long getVideoId() { return videoId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getLecturer() { return lecturer; }
    public void setLecturer(String lecturer) { this.lecturer = lecturer; }
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }
    public String getVideoUrl() { return videoUrl; }
    public void setVideoUrl(String videoUrl) { this.videoUrl = videoUrl; }
    public String getCoverUrl() { return coverUrl; }
    public void setCoverUrl(String coverUrl) { this.coverUrl = coverUrl; }
    public Long getPlayCount() { return playCount; }
    public void setPlayCount(Long playCount) { this.playCount = playCount; }
}
