package com.xinglin.user.service;

import com.xinglin.user.entity.LevelRule;
import com.xinglin.user.repository.LevelRuleRepository;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
public class LevelService {
    private final LevelRuleRepository levelRuleRepository;

    public LevelService(LevelRuleRepository levelRuleRepository) {
        this.levelRuleRepository = levelRuleRepository;
    }

    public int levelOf(long totalPoints) {
        return levelRuleRepository.findByEnabledOrderByMinTotalPointsDesc(1).stream()
                .filter(rule -> totalPoints >= rule.getMinTotalPoints())
                .findFirst()
                .map(LevelRule::getLevel)
                .orElse(1);
    }

    public String nameOf(int level) {
        return levelRuleRepository.findByLevelAndEnabled(level, 1)
                .map(LevelRule::getLevelName)
                .orElse("未配置等级");
    }

    public long nextLevelPoints(int level) {
        List<LevelRule> rules = levelRuleRepository.findByEnabledOrderByLevelAsc(1);
        return rules.stream()
                .filter(rule -> rule.getLevel() > level)
                .min(Comparator.comparing(LevelRule::getLevel))
                .map(LevelRule::getMinTotalPoints)
                .orElseGet(() -> rules.stream()
                        .filter(rule -> rule.getLevel().equals(level))
                        .findFirst()
                        .map(LevelRule::getMinTotalPoints)
                        .orElse(0L));
    }
}
