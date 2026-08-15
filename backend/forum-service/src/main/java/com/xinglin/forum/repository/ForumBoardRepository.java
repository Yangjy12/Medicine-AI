package com.xinglin.forum.repository;

import com.xinglin.forum.entity.ForumBoard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ForumBoardRepository extends JpaRepository<ForumBoard, Long> {
    List<ForumBoard> findByStatusOrderBySortOrderAscIdAsc(String status);
    boolean existsByName(String name);
    boolean existsByIdAndStatus(Long id, String status);

    @Query("select b.id from ForumBoard b where b.status = :status")
    List<Long> findIdsByStatus(@Param("status") String status);
}
