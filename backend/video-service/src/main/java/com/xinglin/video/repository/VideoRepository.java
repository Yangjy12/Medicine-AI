package com.xinglin.video.repository;

import com.xinglin.video.entity.Video;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {
    List<Video> findTop8ByStatusOrderByPublishTimeDesc(String status);

    List<Video> findTop10ByStatusOrderByPlayCountDescLikeCountDescCollectCountDesc(String status);

    List<Video> findByCategoryIdAndStatusAndIdNotOrderByPlayCountDesc(Long categoryId, String status, Long id, Pageable pageable);
}
