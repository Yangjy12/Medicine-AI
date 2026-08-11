package com.xinglin.video.repository;

import com.xinglin.video.entity.LearningRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {
    Optional<LearningRecord> findByUserIdAndVideoId(Long userId, Long videoId);

    Page<LearningRecord> findByUserIdOrderByLastLearnTimeDesc(Long userId, Pageable pageable);
}
