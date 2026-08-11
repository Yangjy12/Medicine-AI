package com.xinglin.video.vo;

public class CategoryVO {
    private Long id;
    private String name;
    private String icon;
    private Integer sort;
    private Long videoCount;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public Integer getSort() { return sort; }
    public void setSort(Integer sort) { this.sort = sort; }
    public Long getVideoCount() { return videoCount; }
    public void setVideoCount(Long videoCount) { this.videoCount = videoCount; }
}
