package com.xinglin.user.config;

import com.xinglin.user.entity.AppUser;
import com.xinglin.user.entity.UserAccount;
import com.xinglin.user.entity.UserProfile;
import com.xinglin.user.repository.AppUserRepository;
import com.xinglin.user.repository.UserAccountRepository;
import com.xinglin.user.repository.UserProfileRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);
    private final AppUserRepository userRepository;
    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public DataInitializer(AppUserRepository userRepository,
                           UserAccountRepository accountRepository,
                           UserProfileRepository profileRepository,
                           BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (userRepository.existsByUsername("student001")) {
            return;
        }
        AppUser user = new AppUser();
        user.setUsername("student001");
        user.setPhone("13800000000");
        user.setNickname("杏林学子");
        user.setAvatar("/assets/avatar/default.png");
        user.setPasswordHash(passwordEncoder.encode("abc123456"));
        AppUser saved = userRepository.save(user);

        UserAccount account = new UserAccount();
        account.setUserId(saved.getId());
        accountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserId(saved.getId());
        profile.setLearningDirection("中医基础理论");
        profile.setBio("正在系统学习中医知识。");
        profileRepository.save(profile);
        log.info("demo user initialized username=student001 password=abc123456 userId={}", saved.getId());
    }
}
