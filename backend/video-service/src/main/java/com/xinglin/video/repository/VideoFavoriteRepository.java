package com.xinglin.video.repository;

import com.xinglin.video.entity.VideoFavorite;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;

public interface VideoFavoriteRepository extends JpaRepository<VideoFavorite, Long> {
    boolean existsByUserIdAndVideoId(Long userId, Long videoId);

    long deleteByUserIdAndVideoId(Long userId, Long videoId);

    void deleteByVideoId(Long videoId);

    Page<VideoFavorite> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    @Query(value = "select f from VideoFavorite f where f.userId = :userId and exists " +
            "(select 1 from Video v where v.id = f.videoId and v.status = :status) order by f.createdAt desc",
            countQuery = "select count(f) from VideoFavorite f where f.userId = :userId and exists " +
                    "(select 1 from Video v where v.id = f.videoId and v.status = :status)")
    Page<VideoFavorite> findByUserIdAndVideoStatusOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                                       @Param("status") String status,
                                                                       Pageable pageable);

    @Query(value = "select f from VideoFavorite f where f.userId = :userId and exists " +
            "(select 1 from Video v where v.id = f.videoId and v.status = :status and v.categoryId in :categoryIds) order by f.createdAt desc",
            countQuery = "select count(f) from VideoFavorite f where f.userId = :userId and exists " +
                    "(select 1 from Video v where v.id = f.videoId and v.status = :status and v.categoryId in :categoryIds)")
    Page<VideoFavorite> findByUserIdAndVideoStatusAndCategoryIdsOrderByCreatedAtDesc(@Param("userId") Long userId,
                                                                                     @Param("status") String status,
                                                                                     @Param("categoryIds") Collection<Long> categoryIds,
                                                                                     Pageable pageable);

    @Modifying
    @Query("delete from VideoFavorite f where f.userId = :userId and not exists " +
            "(select 1 from Video v where v.id = f.videoId)")
    int deleteDanglingByUserId(@Param("userId") Long userId);
}
