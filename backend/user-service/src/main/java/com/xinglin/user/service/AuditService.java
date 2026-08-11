package com.xinglin.user.service;

import com.xinglin.user.entity.AuditLog;
import com.xinglin.user.entity.LoginLog;
import com.xinglin.user.repository.AuditLogRepository;
import com.xinglin.user.repository.LoginLogRepository;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;

@Service
public class AuditService {
    private final AuditLogRepository auditLogRepository;
    private final LoginLogRepository loginLogRepository;

    public AuditService(AuditLogRepository auditLogRepository, LoginLogRepository loginLogRepository) {
        this.auditLogRepository = auditLogRepository;
        this.loginLogRepository = loginLogRepository;
    }

    public void login(Long userId, String account, String result, String failReason, String ip, String userAgent, String deviceId) {
        LoginLog log = new LoginLog();
        log.setUserId(userId);
        log.setAccount(account);
        log.setLoginResult(result);
        log.setFailReason(failReason);
        log.setIp(ip);
        log.setUserAgent(limit(userAgent, 512));
        log.setDeviceId(limit(deviceId, 128));
        log.setTraceId(MDC.get("traceId"));
        loginLogRepository.save(log);
    }

    public void audit(Long userId, String action, String bizId, String result, String ip, String userAgent, String detail) {
        AuditLog log = new AuditLog();
        log.setUserId(userId);
        log.setAction(action);
        log.setBizId(bizId);
        log.setResult(result);
        log.setRequestIp(ip);
        log.setUserAgent(limit(userAgent, 512));
        log.setTraceId(MDC.get("traceId"));
        log.setDetail(limit(detail, 2000));
        auditLogRepository.save(log);
    }

    private String limit(String value, int max) {
        if (value == null || value.length() <= max) {
            return value;
        }
        return value.substring(0, max);
    }
}
