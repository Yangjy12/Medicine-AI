package com.xinglin.user.service;

import com.xinglin.user.common.BusinessException;
import com.xinglin.user.dto.SaveLevelRuleRequest;
import com.xinglin.user.dto.SavePointsRuleRequest;
import com.xinglin.user.entity.LevelRule;
import com.xinglin.user.entity.PointsRule;
import com.xinglin.user.repository.LevelRuleRepository;
import com.xinglin.user.repository.PointsRuleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PointsRuleService {
    private final PointsRuleRepository pointsRuleRepository;
    private final LevelRuleRepository levelRuleRepository;

    public PointsRuleService(PointsRuleRepository pointsRuleRepository, LevelRuleRepository levelRuleRepository) {
        this.pointsRuleRepository = pointsRuleRepository;
        this.levelRuleRepository = levelRuleRepository;
    }

    public PointsRule requireEnabledRule(String bizType) {
        return pointsRuleRepository.findByBizTypeAndEnabled(bizType, 1)
                .orElseThrow(() -> new BusinessException(404, "积分规则未配置：" + bizType));
    }

    public List<PointsRule> listPointsRules() {
        return pointsRuleRepository.findAllByOrderByBizTypeAsc();
    }

    public List<LevelRule> listLevelRules() {
        return levelRuleRepository.findAllByOrderByLevelAsc();
    }

    @Transactional
    public PointsRule savePointsRule(SavePointsRuleRequest request) {
        String bizType = request.getBizType().trim();
        pointsRuleRepository.findByBizType(bizType)
                .filter(rule -> request.getId() == null || !rule.getId().equals(request.getId()))
                .ifPresent(rule -> {
                    throw new BusinessException(409, "积分业务类型已存在：" + bizType);
                });
        PointsRule rule = request.getId() == null
                ? new PointsRule()
                : pointsRuleRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(404, "积分规则不存在"));
        rule.setBizType(bizType);
        rule.setPoints(request.getPoints());
        rule.setDescription(request.getDescription() == null ? null : request.getDescription().trim());
        rule.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
        return pointsRuleRepository.save(rule);
    }

    @Transactional
    public LevelRule saveLevelRule(SaveLevelRuleRequest request) {
        levelRuleRepository.findByLevel(request.getLevel())
                .filter(rule -> request.getId() == null || !rule.getId().equals(request.getId()))
                .ifPresent(rule -> {
                    throw new BusinessException(409, "等级编号已存在：" + request.getLevel());
                });
        LevelRule rule = request.getId() == null
                ? new LevelRule()
                : levelRuleRepository.findById(request.getId()).orElseThrow(() -> new BusinessException(404, "等级规则不存在"));
        rule.setLevel(request.getLevel());
        rule.setLevelName(request.getLevelName().trim());
        rule.setMinTotalPoints(request.getMinTotalPoints());
        rule.setEnabled(request.getEnabled() == null ? 1 : request.getEnabled());
        return levelRuleRepository.save(rule);
    }
}
