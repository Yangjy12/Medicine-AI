package com.xinglin.video.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.xinglin.video.common.BusinessException;
import com.xinglin.video.dto.SaveCategoryRequest;
import com.xinglin.video.entity.VideoCategory;
import com.xinglin.video.repository.VideoCategoryRepository;
import com.xinglin.video.repository.VideoRepository;
import com.xinglin.video.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoCategoryService {
    private static final String CATEGORY_CACHE_KEY = "video:category:list";
    private static final String ONLINE = "ONLINE";

    private final VideoCategoryRepository categoryRepository;
    private final VideoRepository videoRepository;
    private final Cache<String, List<?>> simpleListLocalCache;

    public VideoCategoryService(VideoCategoryRepository categoryRepository,
                                VideoRepository videoRepository,
                                Cache<String, List<?>> simpleListLocalCache) {
        this.categoryRepository = categoryRepository;
        this.videoRepository = videoRepository;
        this.simpleListLocalCache = simpleListLocalCache;
    }

    @SuppressWarnings("unchecked")
    public List<CategoryVO> listEnabledCategories() {
        List<?> cached = simpleListLocalCache.getIfPresent(CATEGORY_CACHE_KEY);
        if (cached != null) {
            return (List<CategoryVO>) cached;
        }
        List<CategoryVO> categories = categoryRepository.findByStatusOrderBySortValueAsc(1).stream()
                .map(this::toVO)
                .collect(Collectors.toList());
        simpleListLocalCache.put(CATEGORY_CACHE_KEY, categories);
        return categories;
    }

    public List<CategoryVO> listAllCategories() {
        return categoryRepository.findAll().stream()
                .map(this::toVO)
                .collect(Collectors.toList());
    }

    public List<Long> enabledCategoryIds() {
        return categoryRepository.findByStatusOrderBySortValueAsc(1).stream()
                .map(VideoCategory::getId)
                .collect(Collectors.toList());
    }

    public CategoryVO saveCategory(SaveCategoryRequest request) {
        VideoCategory category = request.getId() == null
                ? new VideoCategory()
                : categoryRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(404, "分类不存在"));
        category.setName(request.getName().trim());
        category.setIcon(request.getIcon());
        category.setSortValue(request.getSort());
        category.setStatus(request.getStatus() == null ? 1 : request.getStatus());
        VideoCategory saved = categoryRepository.save(category);
        invalidateCache();
        return toVO(saved);
    }

    public void updateStatus(Long categoryId, Integer status) {
        VideoCategory category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BusinessException(404, "分类不存在"));
        category.setStatus(status);
        categoryRepository.save(category);
        invalidateCache();
    }

    public void invalidateCache() {
        simpleListLocalCache.invalidate(CATEGORY_CACHE_KEY);
    }

    public CategoryVO toVO(VideoCategory category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setSort(category.getSortValue());
        vo.setVideoCount(videoRepository.countByCategoryIdAndStatus(category.getId(), ONLINE));
        vo.setStatus(category.getStatus());
        return vo;
    }
}
