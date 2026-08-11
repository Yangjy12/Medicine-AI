package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.common.PageResponse;
import com.xinglin.video.dto.PlayRequest;
import com.xinglin.video.dto.ProgressRequest;
import com.xinglin.video.dto.VideoQueryRequest;
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

    public VideoController(VideoService videoService, VideoCategoryService categoryService) {
        this.videoService = videoService;
        this.categoryService = categoryService;
    }

    @GetMapping("/home")
    public ApiResponse<HomeVO> home(@RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(videoService.home(userId));
    }

    @GetMapping("/categories")
    public ApiResponse<List<CategoryVO>> categories() {
        return ApiResponse.success(categoryService.listEnabledCategories());
    }

    @GetMapping("/list")
    public ApiResponse<PageResponse<VideoCardVO>> list(VideoQueryRequest request,
                                                       @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(videoService.query(request, userId));
    }

    @GetMapping("/search")
    public ApiResponse<PageResponse<VideoCardVO>> search(VideoQueryRequest request,
                                                         @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(videoService.query(request, userId));
    }

    @GetMapping("/{videoId}")
    public ApiResponse<VideoDetailVO> detail(@PathVariable Long videoId,
                                             @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(videoService.detail(videoId, userId));
    }

    @PostMapping("/{videoId}/play")
    public ApiResponse<Void> play(@PathVariable Long videoId,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                  @RequestBody PlayRequest request,
                                  HttpServletRequest servletRequest) {
        videoService.recordPlay(videoId, userId, servletRequest.getRemoteAddr(), request);
        return ApiResponse.success(null);
    }

    @PostMapping("/{videoId}/progress")
    public ApiResponse<ProgressVO> progress(@PathVariable Long videoId,
                                            @RequestHeader(value = "X-User-Id", required = false) Long userId,
                                            @Validated @RequestBody ProgressRequest request) {
        return ApiResponse.success(videoService.updateProgress(videoId, userId, request));
    }

    @PostMapping("/{videoId}/like")
    public ApiResponse<Void> like(@PathVariable Long videoId,
                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        videoService.like(videoId, userId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{videoId}/like")
    public ApiResponse<Void> unlike(@PathVariable Long videoId,
                                    @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        videoService.unlike(videoId, userId);
        return ApiResponse.success(null);
    }

    @PostMapping("/{videoId}/favorite")
    public ApiResponse<Void> favorite(@PathVariable Long videoId,
                                      @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        videoService.favorite(videoId, userId);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{videoId}/favorite")
    public ApiResponse<Void> unfavorite(@PathVariable Long videoId,
                                        @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        videoService.unfavorite(videoId, userId);
        return ApiResponse.success(null);
    }

    @GetMapping("/learning/history")
    public ApiResponse<PageResponse<VideoCardVO>> learningHistory(VideoQueryRequest request,
                                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(videoService.learningHistory(userId, request));
    }

    @GetMapping("/favorites")
    public ApiResponse<PageResponse<VideoCardVO>> favorites(VideoQueryRequest request,
                                                            @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(videoService.favorites(userId, request));
    }

    @GetMapping("/{videoId}/related")
    public ApiResponse<List<VideoCardVO>> related(@PathVariable Long videoId,
                                                  @RequestParam(defaultValue = "6") Integer limit,
                                                  @RequestHeader(value = "X-User-Id", required = false) Long userId) {
        return ApiResponse.success(videoService.related(videoId, limit, userId));
    }
}
