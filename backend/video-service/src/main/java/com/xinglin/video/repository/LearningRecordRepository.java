package com.xinglin.video.repository;

import com.xinglin.video.entity.LearningRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface LearningRecordRepository extends JpaRepository<LearningRecord, Long> {
    Optional<LearningRecord> findByUserIdAndVideoId(Long userId, Long videoId);

    Page<LearningRecord> findByUserIdOrderByLastLearnTimeDesc(Long userId, Pageable pageable);

    @Query(value = "select r from LearningRecord r where r.userId = :userId and exists " +
            "(select 1 from Video v where v.id = r.videoId and v.status = :status) order by r.lastLearnTime desc",
            countQuery = "select count(r) from LearningRecord r where r.userId = :userId and exists " +
                    "(select 1 from Video v where v.id = r.videoId and v.status = :status)")
    Page<LearningRecord> findByUserIdAndVideoStatusOrderByLastLearnTimeDesc(@Param("userId") Long userId,
                                                                            @Param("status") String status,
                                                                            Pageable pageable);

    void deleteByVideoId(Long videoId);
}
