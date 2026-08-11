package com.xinglin.video.repository;

import com.xinglin.video.entity.VideoCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface VideoCategoryRepository extends JpaRepository<VideoCategory, Long> {
    List<VideoCategory> findByStatusOrderBySortValueAsc(Integer status);
}
