package com.xinglin.user.repository;

import com.xinglin.user.entity.PointsRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointsRecordRepository extends JpaRepository<PointsRecord, Long> {
    boolean existsByUserIdAndBizTypeAndBizId(Long userId, String bizType, String bizId);

    Page<PointsRecord> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
}
