package com.xinglin.forum.common;

import java.util.List;

public class PageResponse<T> {
    private List<T> records;
    private int page;
    private int pageSize;
    private long total;

    public PageResponse() {
    }

    public PageResponse(List<T> records, int page, int pageSize, long total) {
        this.records = records;
        this.page = page;
        this.pageSize = pageSize;
        this.total = total;
    }

    public List<T> getRecords() { return records; }
    public void setRecords(List<T> records) { this.records = records; }
    public int getPage() { return page; }
    public void setPage(int page) { this.page = page; }
    public int getPageSize() { return pageSize; }
    public void setPageSize(int pageSize) { this.pageSize = pageSize; }
    public long getTotal() { return total; }
    public void setTotal(long total) { this.total = total; }
}
