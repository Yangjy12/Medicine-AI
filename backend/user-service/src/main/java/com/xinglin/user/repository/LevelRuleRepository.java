package com.xinglin.user.repository;

import com.xinglin.user.entity.LevelRule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LevelRuleRepository extends JpaRepository<LevelRule, Long> {
    List<LevelRule> findByEnabledOrderByMinTotalPointsDesc(Integer enabled);

    List<LevelRule> findByEnabledOrderByLevelAsc(Integer enabled);

    List<LevelRule> findAllByOrderByLevelAsc();

    Optional<LevelRule> findByLevelAndEnabled(Integer level, Integer enabled);

    Optional<LevelRule> findByLevel(Integer level);

    boolean existsByLevel(Integer level);
}
