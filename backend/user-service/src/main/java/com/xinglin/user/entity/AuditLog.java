package com.xinglin.user.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_audit_log", indexes = {
        @Index(name = "idx_audit_user_action_time", columnList = "user_id,action,created_at"),
        @Index(name = "idx_audit_trace", columnList = "trace_id")
})
public class AuditLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(nullable = false, length = 64)
    private String action;
    @Column(name = "biz_id", length = 128)
    private String bizId;
    @Column(nullable = false, length = 32)
    private String result;
    @Column(name = "request_ip", length = 64)
    private String requestIp;
    @Column(name = "user_agent", length = 512)
    private String userAgent;
    @Column(name = "trace_id", length = 64)
    private String traceId;
    @Column(columnDefinition = "TEXT")
    private String detail;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }
    public String getRequestIp() { return requestIp; }
    public void setRequestIp(String requestIp) { this.requestIp = requestIp; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public String getDetail() { return detail; }
    public void setDetail(String detail) { this.detail = detail; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
