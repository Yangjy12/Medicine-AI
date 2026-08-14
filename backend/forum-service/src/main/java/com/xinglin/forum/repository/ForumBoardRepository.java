package com.xinglin.forum.repository;

import com.xinglin.forum.entity.ForumBoard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ForumBoardRepository extends JpaRepository<ForumBoard, Long> {
    List<ForumBoard> findByStatusOrderBySortOrderAscIdAsc(String status);
    boolean existsByName(String name);
}
