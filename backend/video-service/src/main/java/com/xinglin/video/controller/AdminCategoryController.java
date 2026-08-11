package com.xinglin.video.controller;

import com.xinglin.video.common.ApiResponse;
import com.xinglin.video.dto.SaveCategoryRequest;
import com.xinglin.video.service.VideoCategoryService;
import com.xinglin.video.vo.CategoryVO;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/video/admin/categories")
public class AdminCategoryController {
    private final VideoCategoryService categoryService;

    public AdminCategoryController(VideoCategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public ApiResponse<List<CategoryVO>> listAll() {
        return ApiResponse.success(categoryService.listAllCategories());
    }

    @PostMapping
    public ApiResponse<CategoryVO> save(@Validated @RequestBody SaveCategoryRequest request) {
        return ApiResponse.success(categoryService.saveCategory(request));
    }

    @PostMapping("/{categoryId}/enable")
    public ApiResponse<Void> enable(@PathVariable Long categoryId) {
        categoryService.updateStatus(categoryId, 1);
        return ApiResponse.success(null);
    }

    @PostMapping("/{categoryId}/disable")
    public ApiResponse<Void> disable(@PathVariable Long categoryId) {
        categoryService.updateStatus(categoryId, 0);
        return ApiResponse.success(null);
    }
}
