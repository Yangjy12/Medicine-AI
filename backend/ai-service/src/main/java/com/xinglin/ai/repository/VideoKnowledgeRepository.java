package com.xinglin.ai.repository;

import com.xinglin.ai.entity.VideoKnowledge;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VideoKnowledgeRepository extends JpaRepository<VideoKnowledge, Long> {
    @Query("select v from VideoKnowledge v where v.status = 'ONLINE' and " +
            "(v.title like :keyword or v.description like :keyword or v.tags like :keyword or v.lecturer like :keyword) " +
            "order by v.playCount desc, v.likeCount desc, v.collectCount desc")
    List<VideoKnowledge> searchOnline(@Param("keyword") String keyword, Pageable pageable);

    List<VideoKnowledge> findTop8ByStatusOrderByPlayCountDescLikeCountDescCollectCountDesc(String status);
}
