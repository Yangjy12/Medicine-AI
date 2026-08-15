package com.xinglin.forum.repository;

import com.xinglin.forum.entity.ForumLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
}
