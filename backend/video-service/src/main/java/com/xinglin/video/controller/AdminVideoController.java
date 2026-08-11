package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.common.PageResponse;
import com.xinglin.video.dto.SaveVideoRequest;
import com.xinglin.video.dto.VideoQueryRequest;
import com.xinglin.video.service.VideoService;
import com.xinglin.video.vo.VideoCardVO;
import com.xinglin.video.vo.VideoDetailVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video/admin/videos")
public class AdminVideoController {
    private final VideoService videoService;

    public AdminVideoController(VideoService videoService) {
        this.videoService = videoService;
    }

    @GetMapping
    public ApiResponse<PageResponse<VideoCardVO>> list(VideoQueryRequest request,
                                                       @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        return ApiResponse.success(videoService.adminQuery(request, adminId));
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoDetailVO> detail(@PathVariable Long videoId,
                                             @RequestHeader(value = "X-User-Id", required = false) Long adminId) {
        return ApiResponse.success(videoService.adminDetail(videoId, adminId));
    }

    @PostMapping
    public ApiResponse<VideoDetailVO> save(@RequestHeader(value = "X-User-Id", required = false) Long adminId,
                                           @Validated @RequestBody SaveVideoRequest request) {
        return ApiResponse.success(videoService.saveVideo(request, adminId));
    }

    @PostMapping("/{videoId}/online")
    public ApiResponse<Void> online(@PathVariable Long videoId) {
        videoService.updateStatus(videoId, "ONLINE");
        return ApiResponse.success(null);
    }

    @PostMapping("/{videoId}/offline")
    public ApiResponse<Void> offline(@PathVariable Long videoId) {
        videoService.updateStatus(videoId, "OFFLINE");
        return ApiResponse.success(null);
    }
}
