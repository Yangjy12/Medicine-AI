package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.dto.SaveVideoRequest;
import com.xinglin.video.security.AuthenticatedUser;
import com.xinglin.video.security.VideoAuthService;
import com.xinglin.video.service.VideoService;
import com.xinglin.video.vo.VideoDetailVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video/uploads")
public class VideoUploadController {
    private final VideoService videoService;
    private final VideoAuthService authService;

    public VideoUploadController(VideoService videoService, VideoAuthService authService) {
        this.videoService = videoService;
        this.authService = authService;
    }

    @PostMapping
    public ApiResponse<VideoDetailVO> upload(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @Validated @RequestBody SaveVideoRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(videoService.uploadVideo(request, user.getUserId()));
    }
}
