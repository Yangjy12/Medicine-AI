package com.xinglin.video.repository;

import com.xinglin.video.entity.Video;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long>, JpaSpecificationExecutor<Video> {
    List<Video> findTop8ByStatusOrderByPublishTimeDesc(String status);

    List<Video> findTop8ByStatusAndCategoryIdInOrderByPublishTimeDesc(String status, Collection<Long> categoryIds);

    List<Video> findTop10ByStatusOrderByPlayCountDescLikeCountDescCollectCountDesc(String status);

    List<Video> findTop10ByStatusAndCategoryIdInOrderByPlayCountDescLikeCountDescCollectCountDesc(String status, Collection<Long> categoryIds);

    List<Video> findByCategoryIdAndStatusAndIdNotOrderByPlayCountDesc(Long categoryId, String status, Long id, Pageable pageable);

    Page<Video> findByCreatedByOrderByCreatedAtDesc(Long createdBy, Pageable pageable);

    Optional<Video> findByIdAndStatus(Long id, String status);

    long countByCategoryIdAndStatus(Long categoryId, String status);

    @Modifying
    @Query("update Video v set v.playCount = v.playCount + :delta where v.id = :videoId and v.status = :status")
    int increasePlayCount(@Param("videoId") Long videoId, @Param("status") String status, @Param("delta") Long delta);

    @Modifying
    @Query("update Video v set v.likeCount = case when v.likeCount + :delta < 0 then 0 else v.likeCount + :delta end where v.id = :videoId and v.status = :status")
    int increaseLikeCount(@Param("videoId") Long videoId, @Param("status") String status, @Param("delta") Long delta);

    @Modifying
    @Query("update Video v set v.collectCount = case when v.collectCount + :delta < 0 then 0 else v.collectCount + :delta end where v.id = :videoId and v.status = :status")
    int increaseCollectCount(@Param("videoId") Long videoId, @Param("status") String status, @Param("delta") Long delta);
}
