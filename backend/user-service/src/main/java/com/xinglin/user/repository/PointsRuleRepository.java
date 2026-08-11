package com.xinglin.user.repository;

import com.xinglin.user.entity.PointsRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PointsRuleRepository extends JpaRepository<PointsRule, Long> {
    Optional<PointsRule> findByBizTypeAndEnabled(String bizType, Integer enabled);

    Optional<PointsRule> findByBizType(String bizType);

    List<PointsRule> findAllByOrderByBizTypeAsc();

    boolean existsByBizType(String bizType);
}
