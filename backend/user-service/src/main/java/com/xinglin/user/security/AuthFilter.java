package com.xinglin.user.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinglin.user.common.ApiResponse;
import com.xinglin.user.common.BusinessException;
import com.xinglin.user.common.UserContext;
import com.xinglin.user.entity.AppUser;
import com.xinglin.user.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class AuthFilter extends OncePerRequestFilter {
    private static final Logger log = LoggerFactory.getLogger(AuthFilter.class);
    private final JwtService jwtService;
    private final AppUserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final List<String> publicPaths = Arrays.asList(
            "/api/user/register",
            "/api/user/login",
            "/api/user/token/refresh",
            "/actuator/health",
            "/actuator/info"
    );

    public AuthFilter(JwtService jwtService, AppUserRepository userRepository, ObjectMapper objectMapper) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        if (isPublic(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }
        try {
            String authorization = request.getHeader("Authorization");
            if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
                throw new BusinessException(401, "请先登录");
            }
            JwtClaims claims = jwtService.parseAndValidate(authorization.substring(7));
            AppUser user = userRepository.findById(claims.getUserId())
                    .orElseThrow(() -> new BusinessException(401, "用户不存在"));
            if (!"NORMAL".equals(user.getStatus())) {
                throw new BusinessException(403, "账号状态异常");
            }
            if (!user.getTokenVersion().equals(claims.getTokenVersion())) {
                throw new BusinessException(401, "登录状态已失效，请重新登录");
            }
            if (request.getRequestURI().startsWith("/api/user/admin/") && !"ADMIN".equals(user.getRole())) {
                throw new BusinessException(403, "无管理员权限");
            }
            UserContext.setUserId(user.getId());
            MDC.put("userId", String.valueOf(user.getId()));
            filterChain.doFilter(request, response);
        } catch (BusinessException ex) {
            log.warn("auth failed path={} code={} message={}", request.getRequestURI(), ex.getCode(), ex.getMessage());
            writeUnauthorized(response, ex);
        }
    }

    private boolean isPublic(String path) {
        return publicPaths.stream().anyMatch(path::equals) || path.startsWith("/actuator/health");
    }

    private void writeUnauthorized(HttpServletResponse response, BusinessException ex) throws IOException {
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json;charset=UTF-8");
        byte[] body = objectMapper.writeValueAsString(ApiResponse.fail(ex.getCode(), ex.getMessage()))
                .getBytes(StandardCharsets.UTF_8);
        response.getOutputStream().write(body);
    }
}
