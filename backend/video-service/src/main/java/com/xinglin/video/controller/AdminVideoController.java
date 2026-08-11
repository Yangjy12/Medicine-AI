package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.common.PageResponse;
import com.xinglin.video.dto.SaveVideoRequest;
import com.xinglin.video.dto.VideoQueryRequest;
import com.xinglin.video.security.AuthenticatedUser;
import com.xinglin.video.security.VideoAuthService;
import com.xinglin.video.service.VideoService;
import com.xinglin.video.vo.VideoCardVO;
import com.xinglin.video.vo.VideoDetailVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video/admin/videos")
public class AdminVideoController {
    private final VideoService videoService;
    private final VideoAuthService authService;

    public AdminVideoController(VideoService videoService, VideoAuthService authService) {
        this.videoService = videoService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<PageResponse<VideoCardVO>> list(VideoQueryRequest request,
                                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser admin = authService.requireAdmin(authorization);
        return ApiResponse.success(videoService.adminQuery(request, admin.getUserId()));
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoDetailVO> detail(@PathVariable Long videoId,
                                             @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser admin = authService.requireAdmin(authorization);
        return ApiResponse.success(videoService.adminDetail(videoId, admin.getUserId()));
    }

    @PostMapping
    public ApiResponse<VideoDetailVO> save(@RequestHeader(value = "Authorization", required = false) String authorization,
                                           @Validated @RequestBody SaveVideoRequest request) {
        AuthenticatedUser admin = authService.requireAdmin(authorization);
        return ApiResponse.success(videoService.saveVideo(request, admin.getUserId()));
    }

    @PostMapping("/{videoId}/online")
    public ApiResponse<Void> online(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @PathVariable Long videoId) {
        authService.requireAdmin(authorization);
        videoService.updateStatus(videoId, "ONLINE");
        return ApiResponse.success(null);
    }

    @PostMapping("/{videoId}/offline")
    public ApiResponse<Void> offline(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @PathVariable Long videoId) {
        authService.requireAdmin(authorization);
        videoService.updateStatus(videoId, "OFFLINE");
        return ApiResponse.success(null);
    }
}
