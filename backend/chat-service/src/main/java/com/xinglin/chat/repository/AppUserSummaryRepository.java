package com.xinglin.chat.repository;

import com.xinglin.chat.entity.AppUserSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AppUserSummaryRepository extends JpaRepository<AppUserSummary, Long> {
    boolean existsByIdAndStatus(Long id, String status);

    List<AppUserSummary> findByIdInAndStatus(Collection<Long> ids, String status);
}
