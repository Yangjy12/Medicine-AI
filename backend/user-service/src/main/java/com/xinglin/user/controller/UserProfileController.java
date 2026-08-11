package com.xinglin.user.controller;

import com.xinglin.user.common.ApiResponse;
import com.xinglin.user.common.RequestTraceFilter;
import com.xinglin.user.common.UserContext;
import com.xinglin.user.dto.ProfileUpdateRequest;
import com.xinglin.user.service.ProfileService;
import com.xinglin.user.vo.UserVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/user/profile")
public class UserProfileController {
    private final ProfileService profileService;

    public UserProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @PutMapping
    public ApiResponse<UserVO> update(@Validated @RequestBody ProfileUpdateRequest request, HttpServletRequest servletRequest) {
        return ApiResponse.success(profileService.update(
                UserContext.getUserId(),
                request,
                RequestTraceFilter.clientIp(servletRequest),
                servletRequest.getHeader("User-Agent")));
    }
}
