package com.xinglin.video.dto;

public class VideoQueryRequest {
    private String keyword;
    private Long categoryId;
    private String tag;
    private String sort = "comprehensive";
    private Integer minDuration;
    private Integer maxDuration;
    private Integer page = 1;
    private Integer pageSize = 12;

    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public Integer getMinDuration() { return minDuration; }
    public void setMinDuration(Integer minDuration) { this.minDuration = minDuration; }
    public Integer getMaxDuration() { return maxDuration; }
    public void setMaxDuration(Integer maxDuration) { this.maxDuration = maxDuration; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
}
