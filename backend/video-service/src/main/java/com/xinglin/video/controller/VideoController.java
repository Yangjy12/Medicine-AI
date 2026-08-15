package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.common.PageResponse;
import com.xinglin.video.dto.PlayRequest;
import com.xinglin.video.dto.ProgressRequest;
import com.xinglin.video.dto.VideoQueryRequest;
import com.xinglin.video.security.AuthenticatedUser;
import com.xinglin.video.security.VideoAuthService;
import com.xinglin.video.service.VideoCategoryService;
import com.xinglin.video.service.VideoService;
import com.xinglin.video.vo.*;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@RestController
@RequestMapping("/api/video")
public class VideoController {
    private final VideoService videoService;
    private final VideoCategoryService categoryService;
    private final VideoAuthService authService;

    public VideoController(VideoService videoService, VideoCategoryService categoryService, VideoAuthService authService) {
        this.videoService = videoService;
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeVO> home(@RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(videoService.home(authService.optionalUserId(authorization)));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryVO>> categories() {
        return ApiResponse.success(categoryService.listEnabledCategories());
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<VideoCardVO>> list(VideoQueryRequest request,
                                                       @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(videoService.query(request, authService.optionalUserId(authorization)));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<VideoCardVO>> search(VideoQueryRequest request,
                                                         @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(videoService.query(request, authService.optionalUserId(authorization)));
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoDetailVO> detail(@PathVariable Long videoId,
                                             @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(videoService.detail(videoId, authService.optionalUserId(authorization)));
    }

    @PostMapping("/{videoId}/play")
    public ApiResponse<Void> play(@PathVariable Long videoId,
                                  @RequestHeader(value = "Authorization", required = false) String authorization,
                                  @RequestBody PlayRequest request,
                                  HttpServletRequest servletRequest) {
        videoService.recordPlay(videoId, authService.optionalUserId(authorization), servletRequest.getRemoteAddr(), request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{videoId}/progress")
    public ApiResponse<ProgressVO> progress(@PathVariable Long videoId,
                                            @RequestHeader(value = "Authorization", required = false) String authorization,
                                            @Validated @RequestBody ProgressRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(videoService.updateProgress(videoId, user.getUserId(), request));
    }

    @PostMapping("/{videoId}/like")
    public ApiResponse<Void> like(@PathVariable Long videoId,
                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        videoService.like(videoId, user.getUserId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{videoId}/like")
    public ApiResponse<Void> unlike(@PathVariable Long videoId,
                                    @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        videoService.unlike(videoId, user.getUserId());
        return ApiResponse.success(null);
    }

    @PostMapping("/{videoId}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long videoId,
                                      @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        videoService.favorite(videoId, user.getUserId());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{videoId}/favorite")
    public ApiResponse<Void> unfavorite(@PathVariable Long videoId,
                                        @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        videoService.unfavorite(videoId, user.getUserId());
        return ApiResponse.success(null);
    }

    @GetMapping("/learning/history")
    public ApiResponse<PageResponse<VideoCardVO>> learningHistory(VideoQueryRequest request,
                                                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(videoService.learningHistory(user.getUserId(), request));
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<VideoCardVO>> favorites(VideoQueryRequest request,
                                                            @RequestHeader(value = "Authorization", required = false) String authorization) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(videoService.favorites(user.getUserId(), request));
    }

    @GetMapping("/{videoId}/related")
    public ApiResponse<List<VideoCardVO>> related(@PathVariable Long videoId,
                                                  @RequestParam(defaultValue = "6") Integer limit,
                                                  @RequestHeader(value = "Authorization", required = false) String authorization) {
        return ApiResponse.success(videoService.related(videoId, limit, authService.optionalUserId(authorization)));
    }
}
