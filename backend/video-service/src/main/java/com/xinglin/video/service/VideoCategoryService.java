package com.xinglin.video.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.xinglin.video.entity.VideoCategory;
import com.xinglin.video.repository.VideoCategoryRepository;
import com.xinglin.video.vo.CategoryVO;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VideoCategoryService {
    private static final String CATEGORY_CACHE_KEY = "video:category:list";

    private final VideoCategoryRepository categoryRepository;
    private final Cache<String, List<?>> simpleListLocalCache;

    public VideoCategoryService(VideoCategoryRepository categoryRepository, Cache<String, List<?>> simpleListLocalCache) {
        this.categoryRepository = categoryRepository;
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

    private CategoryVO toVO(VideoCategory category) {
        CategoryVO vo = new CategoryVO();
        vo.setId(category.getId());
        vo.setName(category.getName());
        vo.setIcon(category.getIcon());
        vo.setSort(category.getSortValue());
        vo.setVideoCount(0L);
        return vo;
    }
}
