package com.xinglin.forum.repository;

import com.xinglin.forum.entity.ForumFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ForumFavoriteRepository extends JpaRepository<ForumFavorite, Long> {
    boolean existsByUserIdAndPostId(Long userId, Long postId);
    long deleteByUserIdAndPostId(Long userId, Long postId);
    Page<ForumFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
