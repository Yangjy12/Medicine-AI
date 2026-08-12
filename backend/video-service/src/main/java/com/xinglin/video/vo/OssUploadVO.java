package com.xinglin.video.vo;

public class OssUploadVO {
    private String objectKey;
    private String url;
    private String originalFilename;
    private String contentType;
    private Long size;

    public OssUploadVO() {
    }

    public OssUploadVO(String objectKey, String url, String originalFilename, String contentType, Long size) {
        this.objectKey = objectKey;
        this.url = url;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.size = size;
    }

    public String getObjectKey() { return objectKey; }
    public void setObjectKey(String objectKey) { this.objectKey = objectKey; }
    public String getUrl() { return url; }
    public void setUrl(String url) { this.url = url; }
    public String getOriginalFilename() { return originalFilename; }
    public void setOriginalFilename(String originalFilename) { this.originalFilename = originalFilename; }
    public String getContentType() { return contentType; }
    public void setContentType(String contentType) { this.contentType = contentType; }
    public Long getSize() { return size; }
    public void setSize(Long size) { this.size = size; }
}
