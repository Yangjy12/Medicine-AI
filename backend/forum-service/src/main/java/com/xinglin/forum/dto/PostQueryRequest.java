package com.xinglin.forum.dto;

public class PostQueryRequest {
    private Long boardId;
    private String keyword;
    private String sort = "latest";
    private Integer page = 1;
    private Integer pageSize = 12;
    private String status;

    public Long getBoardId() { return boardId; }
    public void setBoardId(Long boardId) { this.boardId = boardId; }
    public String getKeyword() { return keyword; }
    public void setKeyword(String keyword) { this.keyword = keyword; }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
    public Integer getPage() { return page; }
    public void setPage(Integer page) { this.page = page; }
    public Integer getPageSize() { return pageSize; }
    public void setPageSize(Integer pageSize) { this.pageSize = pageSize; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
