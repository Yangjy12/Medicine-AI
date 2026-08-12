package com.xinglin.video.repository;

import com.xinglin.video.entity.VideoFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoFavoriteRepository extends JpaRepository<VideoFavorite, Long> {
    boolean existsByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByVideoId(Long videoId);

    Page<VideoFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
