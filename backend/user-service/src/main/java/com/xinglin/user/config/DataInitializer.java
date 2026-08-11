package com.xinglin.user.config;

import com.xinglin.user.entity.*;
import com.xinglin.user.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final AppUserRepository userRepository;
    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final PointsRuleRepository pointsRuleRepository;
    private final LevelRuleRepository levelRuleRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${xinglin.seed.demo-user-enabled:true}")
    private boolean demoUserEnabled;
    @Value("${xinglin.seed.demo-username:student001}")
    private String demoUsername;
    @Value("${xinglin.seed.demo-password:abc123456}")
    private String demoPassword;
    @Value("${xinglin.seed.demo-phone:13800000000}")
    private String demoPhone;
    @Value("${xinglin.seed.demo-nickname:杏林学子}")
    private String demoNickname;
    @Value("${xinglin.seed.admin-username:yjyjocyer}")
    private String adminUsername;
    @Value("${xinglin.seed.admin-password:}")
    private String adminPassword;
    @Value("${xinglin.seed.admin-phone:13900000000}")
    private String adminPhone;

    public DataInitializer(AppUserRepository userRepository,
                           UserAccountRepository accountRepository,
                           UserProfileRepository profileRepository,
                           PointsRuleRepository pointsRuleRepository,
                           LevelRuleRepository levelRuleRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.pointsRuleRepository = pointsRuleRepository;
        this.levelRuleRepository = levelRuleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        backfillUserRoles();
        initPointsRules();
        initLevelRules();
        initAdminUser();
        initDemoUser();
    }

    private void backfillUserRoles() {
        userRepository.findAll().stream()
                .filter(user -> !StringUtils.hasText(user.getRole()))
                .forEach(user -> {
                    user.setRole("USER");
                    userRepository.save(user);
                });
    }

    private void initAdminUser() {
        if (!StringUtils.hasText(adminUsername)) {
            return;
        }
        userRepository.findByUsername(adminUsername.trim()).ifPresentOrElse(existing -> {
            if (!"ADMIN".equals(existing.getRole())) {
                existing.setRole("ADMIN");
                userRepository.save(existing);
                log.info("admin role granted username={} userId={}", existing.getUsername(), existing.getId());
            }
        }, () -> {
            if (!StringUtils.hasText(adminPassword)) {
                log.warn("admin user seed skipped because ADMIN_PASSWORD is not configured username={}", adminUsername.trim());
                return;
            }
            AppUser user = new AppUser();
            user.setUsername(adminUsername.trim());
            user.setPhone(StringUtils.hasText(adminPhone) ? adminPhone.trim() : null);
            user.setNickname(adminUsername.trim());
            user.setAvatar("/assets/avatar/admin.png");
            user.setRole("ADMIN");
            user.setPasswordHash(passwordEncoder.encode(adminPassword));
            AppUser saved = userRepository.save(user);

            UserAccount account = new UserAccount();
            account.setUserId(saved.getId());
            accountRepository.save(account);

            UserProfile profile = new UserProfile();
            profile.setUserId(saved.getId());
            profile.setLearningDirection("平台运营管理");
            profile.setBio("杏林学堂管理员账号。");
            profileRepository.save(profile);
            log.info("admin user initialized username={} userId={}", saved.getUsername(), saved.getId());
        });
    }

    private void initDemoUser() {
        if (!demoUserEnabled) {
            log.info("demo user seed skipped by configuration");
            return;
        }
        if (!StringUtils.hasText(demoUsername) || userRepository.existsByUsername(demoUsername)) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername(demoUsername.trim());
        user.setPhone(StringUtils.hasText(demoPhone) ? demoPhone.trim() : null);
        user.setNickname(StringUtils.hasText(demoNickname) ? demoNickname.trim() : demoUsername.trim());
        user.setAvatar("/assets/avatar/default.png");
        user.setRole("USER");
        user.setPasswordHash(passwordEncoder.encode(demoPassword));
        AppUser saved = userRepository.save(user);

        UserAccount account = new UserAccount();
        account.setUserId(saved.getId());
        accountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserId(saved.getId());
        profile.setLearningDirection("中医基础理论");
        profile.setBio("正在系统学习中医知识。");
        profileRepository.save(profile);
        log.info("demo user initialized username={} userId={}", saved.getUsername(), saved.getId());
    }

    private void initPointsRules() {
        if (pointsRuleRepository.count() > 0) {
            return;
        }
        savePointsRule("CHECKIN", 5, "每日签到");
        savePointsRule("CHECKIN_7_DAYS", 20, "连续签到7天奖励");
        savePointsRule("VIDEO_FINISH", 10, "完成视频学习");
        savePointsRule("POST_CREATE", 5, "发布帖子");
        savePointsRule("COMMENT_CREATE", 2, "发表评论");
        log.info("default points rules initialized");
    }

    private void savePointsRule(String bizType, int points, String description) {
        PointsRule rule = new PointsRule();
        rule.setBizType(bizType);
        rule.setPoints(points);
        rule.setDescription(description);
        rule.setEnabled(1);
        pointsRuleRepository.save(rule);
    }

    private void initLevelRules() {
        if (levelRuleRepository.count() > 0) {
            return;
        }
        saveLevelRule(1, "初入杏林", 0);
        saveLevelRule(2, "闻道学子", 100);
        saveLevelRule(3, "经方研习者", 500);
        saveLevelRule(4, "岐黄进阶者", 1500);
        saveLevelRule(5, "杏林达人", 4000);
        log.info("default level rules initialized");
    }

    private void saveLevelRule(int level, String name, long minTotalPoints) {
        LevelRule rule = new LevelRule();
        rule.setLevel(level);
        rule.setLevelName(name);
        rule.setMinTotalPoints(minTotalPoints);
        rule.setEnabled(1);
        levelRuleRepository.save(rule);
    }
}
