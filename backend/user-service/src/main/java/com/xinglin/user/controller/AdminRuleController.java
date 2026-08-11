package com.xinglin.user.controller;

import com.xinglin.user.common.ApiResponse;
import com.xinglin.user.dto.SaveLevelRuleRequest;
import com.xinglin.user.dto.SavePointsRuleRequest;
import com.xinglin.user.entity.LevelRule;
import com.xinglin.user.entity.PointsRule;
import com.xinglin.user.service.PointsRuleService;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user/admin/rules")
public class AdminRuleController {
    private final PointsRuleService ruleService;

    public AdminRuleController(PointsRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping("/points")
    public ApiResponse<List<PointsRule>> pointsRules() {
        return ApiResponse.success(ruleService.listPointsRules());
    }

    @PostMapping("/points")
    public ApiResponse<PointsRule> savePointsRule(@Validated @RequestBody SavePointsRuleRequest request) {
        return ApiResponse.success(ruleService.savePointsRule(request));
    }

    @GetMapping("/levels")
    public ApiResponse<List<LevelRule>> levelRules() {
        return ApiResponse.success(ruleService.listLevelRules());
    }

    @PostMapping("/levels")
    public ApiResponse<LevelRule> saveLevelRule(@Validated @RequestBody SaveLevelRuleRequest request) {
        return ApiResponse.success(ruleService.saveLevelRule(request));
    }
}
