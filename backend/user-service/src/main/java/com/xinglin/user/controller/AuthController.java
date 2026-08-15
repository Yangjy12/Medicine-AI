package com.xinglin.user.controller;

import com.xinglin.user.common.ApiResponse;
import com.xinglin.user.common.RequestTraceFilter;
import com.xinglin.user.common.UserContext;
import com.xinglin.user.dto.LoginRequest;
import com.xinglin.user.dto.RefreshTokenRequest;
import com.xinglin.user.dto.RegisterRequest;
import com.xinglin.user.service.AuthService;
import com.xinglin.user.vo.LoginVO;
import com.xinglin.user.vo.TokenVO;
import com.xinglin.user.vo.UserVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ApiResponse<UserVO> register(@Validated @RequestBody RegisterRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.register(request, RequestTraceFilter.clientIp(servletRequest), servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/login")
    public ApiResponse<LoginVO> login(@Validated @RequestBody LoginRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(authService.login(request, RequestTraceFilter.clientIp(servletRequest), servletRequest.getHeader("User-Agent")));
    }

    @PostMapping("/token/refresh")
    public ApiResponse<TokenVO> refresh(@Validated @RequestBody RefreshTokenRequest request) {
        return ApiResponse.success(authService.refresh(request));
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@RequestParam(defaultValue = "web") String deviceId,
                                    @RequestHeader(value = "Authorization", required = false) String authorization,
                                    HttpServletRequest servletRequest) {
        authService.logout(UserContext.getUserId(), deviceId, authorization,
                RequestTraceFilter.clientIp(servletRequest), servletRequest.getHeader("User-Agent"));
        return ApiResponse.success(null);
    }

    @GetMapping("/me")
    public ApiResponse<UserVO> me() {
        return ApiResponse.success(authService.me(UserContext.getUserId()));
    }
}
