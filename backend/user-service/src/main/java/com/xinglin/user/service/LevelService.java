package com.xinglin.user.service;

import org.springframework.stereotype.Service;

@Service
public class LevelService {
    public int levelOf(long totalPoints) {
        if (totalPoints >= 4000) return 5;
        if (totalPoints >= 1500) return 4;
        if (totalPoints >= 500) return 3;
        if (totalPoints >= 100) return 2;
        return 1;
    }

    public String nameOf(int level) {
        switch (level) {
            case 5: return "杏林达人";
            case 4: return "岐黄进阶者";
            case 3: return "经方研习者";
            case 2: return "闻道学子";
            default: return "初入杏林";
        }
    }

    public long nextLevelPoints(int level) {
        switch (level) {
            case 1: return 100;
            case 2: return 500;
            case 3: return 1500;
            case 4: return 4000;
            default: return 4000;
        }
    }
}
