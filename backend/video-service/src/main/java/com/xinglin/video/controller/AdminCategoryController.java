package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.dto.SaveCategoryRequest;
import com.xinglin.video.security.VideoAuthService;
import com.xinglin.video.service.VideoCategoryService;
import com.xinglin.video.vo.CategoryVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video/admin/categories")
public class AdminCategoryController {
    private final VideoCategoryService categoryService;
    private final VideoAuthService authService;

    public AdminCategoryController(VideoCategoryService categoryService, VideoAuthService authService) {
        this.categoryService = categoryService;
        this.authService = authService;
    }

    @GetMapping
    public ApiResponse<List<CategoryVO>> listAll(@RequestHeader(value = "Authorization", required = false) String authorization) {
        authService.requireAdmin(authorization);
        return ApiResponse.success(categoryService.listAllCategories());
    }

    @PostMapping
    public ApiResponse<CategoryVO> save(@RequestHeader(value = "Authorization", required = false) String authorization,
                                        @Validated @RequestBody SaveCategoryRequest request) {
        authService.requireAdmin(authorization);
        return ApiResponse.success(categoryService.saveCategory(request));
    }

    @PostMapping("/{categoryId}/enable")
    public ApiResponse<Void> enable(@RequestHeader(value = "Authorization", required = false) String authorization,
                                    @PathVariable Long categoryId) {
        authService.requireAdmin(authorization);
        categoryService.updateStatus(categoryId, 1);
        return ApiResponse.success(null);
    }

    @PostMapping("/{categoryId}/disable")
    public ApiResponse<Void> disable(@RequestHeader(value = "Authorization", required = false) String authorization,
                                     @PathVariable Long categoryId) {
        authService.requireAdmin(authorization);
        categoryService.updateStatus(categoryId, 0);
        return ApiResponse.success(null);
    }
}
