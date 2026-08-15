package com.xinglin.forum.repository;

import com.xinglin.forum.entity.ForumPost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ForumPostRepository extends JpaRepository<ForumPost, Long>, JpaSpecificationExecutor<ForumPost> {
    Page<ForumPost> findByUserIdAndStatusNotOrderByCreatedAtDesc(Long userId, String status, Pageable pageable);
    List<ForumPost> findTop8ByStatusOrderByHotScoreDescPublishTimeDesc(String status);

    @Override
    Page<ForumPost> findAll(Specification<ForumPost> specification, Pageable pageable);

    @Modifying
    @Query("update ForumPost p set p.viewCount = p.viewCount + :delta, p.hotScore = p.hotScore + :delta where p.id = :postId")
    int increaseViewCount(Long postId, Long delta);

    @Modifying
    @Query("update ForumPost p set p.commentCount = case when p.commentCount + :delta < 0 then 0 else p.commentCount + :delta end, " +
            "p.hotScore = case when p.hotScore + (:delta * 5) < 0 then 0 else p.hotScore + (:delta * 5) end where p.id = :postId")
    int increaseCommentCount(Long postId, Long delta);

    @Modifying
    @Query("update ForumPost p set p.likeCount = case when p.likeCount + :delta < 0 then 0 else p.likeCount + :delta end, " +
            "p.hotScore = case when p.hotScore + (:delta * 3) < 0 then 0 else p.hotScore + (:delta * 3) end where p.id = :postId")
    int increaseLikeCount(Long postId, Long delta);

    @Modifying
    @Query("update ForumPost p set p.favoriteCount = case when p.favoriteCount + :delta < 0 then 0 else p.favoriteCount + :delta end, " +
            "p.hotScore = case when p.hotScore + (:delta * 4) < 0 then 0 else p.hotScore + (:delta * 4) end where p.id = :postId")
    int increaseFavoriteCount(Long postId, Long delta);
}
