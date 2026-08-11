package com.xinglin.video.security;

import com.xinglin.video.common.BusinessException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class VideoAuthService {
    private final VideoJwtService jwtService;

    public VideoAuthService(VideoJwtService jwtService) {
        this.jwtService = jwtService;
    }

    public AuthenticatedUser requireLogin(String authorization) {
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            throw new BusinessException(401, "请先登录");
        }
        return jwtService.parseAndValidate(authorization.substring(7));
    }

    public AuthenticatedUser requireAdmin(String authorization) {
        AuthenticatedUser user = requireLogin(authorization);
        if (!user.hasRole("ADMIN")) {
            throw new BusinessException(403, "无管理员权限");
        }
        return user;
    }
}
