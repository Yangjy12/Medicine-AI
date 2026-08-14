package com.xinglin.forum.security;

import com.xinglin.forum.common.BusinessException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ForumAuthService {
    private final ForumJwtService jwtService;

    public ForumAuthService(ForumJwtService jwtService) {
        this.jwtService = jwtService;
    }

    public Long optionalUserId(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return null;
        }
        AuthenticatedUser user = jwtService.parseAndValidate(authorization.substring(7));
        MDC.put("userId", String.valueOf(user.getUserId()));
        return user.getUserId();
    }

    public AuthenticatedUser requireLogin(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        AuthenticatedUser user = jwtService.parseAndValidate(authorization.substring(7));
        MDC.put("userId", String.valueOf(user.getUserId()));
        return user;
    }

    public AuthenticatedUser requireAdmin(String authorization) {
        AuthenticatedUser user = requireLogin(authorization);
        if (!user.hasRole("ADMIN")) {
            throw new BusinessException(403, "无管理员权限");
        }
        return user;
    }
}
