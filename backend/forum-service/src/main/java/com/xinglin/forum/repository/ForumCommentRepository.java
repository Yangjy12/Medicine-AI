package com.xinglin.forum.repository;

import com.xinglin.forum.entity.ForumComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Collection;
import java.util.List;

public interface ForumCommentRepository extends JpaRepository<ForumComment, Long> {
    Page<ForumComment> findByPostIdAndParentIdAndStatusOrderByCreatedAtDesc(Long postId, Long parentId, String status, Pageable pageable);
    Page<ForumComment> findByPostIdAndRootIdAndParentIdNotAndStatusOrderByCreatedAtAsc(Long postId, Long rootId, Long parentId, String status, Pageable pageable);
    List<ForumComment> findByPostIdAndRootIdInAndParentIdNotAndStatusOrderByCreatedAtAsc(Long postId, Collection<Long> rootIds, Long parentId, String status);

    @Modifying
    @Query("update ForumComment c set c.likeCount = c.likeCount + :delta where c.id = :commentId")
    int increaseLikeCount(Long commentId, Long delta);
}
