package com.xinglin.chat.security;

import com.xinglin.chat.common.BusinessException;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class ChatAuthService {
    private final ChatJwtService jwtService;

    public ChatAuthService(ChatJwtService jwtService) {
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

    public AuthenticatedUser requireToken(String token) {
        if (!StringUtils.hasText(token)) {
            throw new BusinessException(401, "请先登录");
        }
        return jwtService.parseAndValidate(token);
    }
}
