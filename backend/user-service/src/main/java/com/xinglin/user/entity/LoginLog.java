package com.xinglin.user.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_login_log", indexes = {
        @Index(name = "idx_login_user_time", columnList = "user_id,created_at"),
        @Index(name = "idx_login_account_time", columnList = "account,created_at"),
        @Index(name = "idx_login_ip_time", columnList = "ip,created_at")
})
public class LoginLog {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id")
    private Long userId;
    @Column(length = 128)
    private String account;
    @Column(name = "login_result", nullable = false, length = 32)
    private String loginResult;
    @Column(name = "fail_reason", length = 255)
    private String failReason;
    @Column(length = 64)
    private String ip;
    @Column(name = "user_agent", length = 512)
    private String userAgent;
    @Column(name = "device_id", length = 128)
    private String deviceId;
    @Column(name = "trace_id", length = 64)
    private String traceId;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getAccount() { return account; }
    public void setAccount(String account) { this.account = account; }
    public String getLoginResult() { return loginResult; }
    public void setLoginResult(String loginResult) { this.loginResult = loginResult; }
    public String getFailReason() { return failReason; }
    public void setFailReason(String failReason) { this.failReason = failReason; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getUserAgent() { return userAgent; }
    public void setUserAgent(String userAgent) { this.userAgent = userAgent; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
    public String getTraceId() { return traceId; }
    public void setTraceId(String traceId) { this.traceId = traceId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
