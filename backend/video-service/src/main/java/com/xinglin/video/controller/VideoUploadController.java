package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.common.PageResponse;
import com.xinglin.video.dto.SaveVideoRequest;
import com.xinglin.video.dto.VideoQueryRequest;
import com.xinglin.video.security.AuthenticatedUser;
import com.xinglin.video.security.VideoAuthService;
import com.xinglin.video.service.OssStorageService;
import com.xinglin.video.service.VideoService;
import com.xinglin.video.vo.VideoCardVO;
import com.xinglin.video.vo.VideoDetailVO;
import com.xinglin.video.vo.OssUploadVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/video/uploads")
public class VideoUploadController {
    private final VideoService videoService;
    private final VideoAuthService authService;
    private final OssStorageService ossStorageService;

    public VideoUploadController(VideoService videoService, VideoAuthService authService, OssStorageService ossStorageService) {
        this.videoService = videoService;
        this.authService = authService;
        this.ossStorageService = ossStorageService;
    }

    @GetMapping
    public ApiResponse<PageResponse<VideoCardVO>> list(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                       VideoQueryRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(videoService.myUploads(user.getUserId(), request));
    }

    @PostMapping
    public ApiResponse<VideoDetailVO> upload(@RequestHeader(value = "Authorization", required = false) String authorization,
                                             @Validated @RequestBody SaveVideoRequest request) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(videoService.uploadVideo(request, user.getUserId()));
    }

    @PostMapping("/file")
    public ApiResponse<OssUploadVO> uploadVideoFile(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                    @RequestParam("file") MultipartFile file) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(ossStorageService.uploadVideo(file, user.getUserId()));
    }

    @PostMapping("/cover")
    public ApiResponse<OssUploadVO> uploadCover(@RequestHeader(value = "Authorization", required = false) String authorization,
                                                @RequestParam("file") MultipartFile file) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        return ApiResponse.success(ossStorageService.uploadCover(file, user.getUserId()));
    }

    @DeleteMapping("/{videoId}")
    public ApiResponse<Void> delete(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @PathVariable Long videoId) {
        AuthenticatedUser user = authService.requireLogin(authorization);
        videoService.deleteUpload(videoId, user.getUserId());
        return ApiResponse.success(null);
    }
}
