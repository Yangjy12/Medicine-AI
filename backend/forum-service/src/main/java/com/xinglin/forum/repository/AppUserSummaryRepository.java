package com.xinglin.forum.repository;

import com.xinglin.forum.entity.AppUserSummary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface AppUserSummaryRepository extends JpaRepository<AppUserSummary, Long> {
    List<AppUserSummary> findByIdInAndStatus(Collection<Long> ids, String status);
}
