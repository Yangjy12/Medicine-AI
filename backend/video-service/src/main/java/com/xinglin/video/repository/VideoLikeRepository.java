package com.xinglin.video.repository;

import com.xinglin.video.entity.VideoLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VideoLikeRepository extends JpaRepository<VideoLike, Long> {
    boolean existsByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByVideoId(Long videoId);
}
