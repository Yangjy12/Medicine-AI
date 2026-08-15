package com.xinglin.forum.repository;

import com.xinglin.forum.entity.ForumLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;

public interface ForumLikeRepository extends JpaRepository<ForumLike, Long> {
    boolean existsByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long deleteByUserIdAndTargetTypeAndTargetId(Long userId, String targetType, Long targetId);
    long deleteByTargetTypeAndTargetId(String targetType, Long targetId);
    long deleteByTargetTypeAndTargetIdIn(String targetType, Collection<Long> targetIds);
}
