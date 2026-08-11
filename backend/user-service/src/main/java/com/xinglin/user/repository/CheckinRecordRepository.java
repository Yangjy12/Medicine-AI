package com.xinglin.user.repository;

import com.xinglin.user.entity.CheckinRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;

public interface CheckinRecordRepository extends JpaRepository<CheckinRecord, Long> {
    boolean existsByUserIdAndCheckinDate(Long userId, LocalDate checkinDate);

    long countByUserId(Long userId);
}
