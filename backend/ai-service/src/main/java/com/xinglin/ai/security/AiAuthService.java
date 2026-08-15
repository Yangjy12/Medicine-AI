package com.xinglin.ai.security;

import com.xinglin.ai.common.BusinessException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class AiAuthService {
    private final AiJwtService jwtService;

    public AiAuthService(AiJwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthenticatedUser requireLogin(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        AuthenticatedUser user = jwtService.parseAndValidate(authorization.substring(7));
        MDC.put("userId", String.valueOf(user.getUserId()));
        return user;
    }
}
