package com.xinglin.user.service;

import com.xinglin.user.common.BusinessException;
import com.xinglin.user.dto.ProfileUpdateRequest;
import com.xinglin.user.entity.AppUser;
import com.xinglin.user.entity.UserProfile;
import com.xinglin.user.repository.AppUserRepository;
import com.xinglin.user.repository.UserProfileRepository;
import com.xinglin.user.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class ProfileService {
    private static final Logger log = LoggerFactory.getLogger(ProfileService.class);
    private final AppUserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserAssembler userAssembler;
    private final AuditService auditService;

    public ProfileService(AppUserRepository userRepository,
                          UserProfileRepository profileRepository,
                          UserAssembler userAssembler,
                          AuditService auditService) {
        this.userRepository = userRepository;
        this.profileRepository = profileRepository;
        this.userAssembler = userAssembler;
        this.auditService = auditService;
    }

    @Transactional
    public UserVO update(Long userId, ProfileUpdateRequest request, String ip, String userAgent) {
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        if (StringUtils.hasText(request.getNickname())) {
            user.setNickname(clean(request.getNickname()));
        }
        if (StringUtils.hasText(request.getAvatar())) {
            user.setAvatar(clean(request.getAvatar()));
        }
        userRepository.save(user);

        UserProfile profile = profileRepository.findByUserId(userId).orElseGet(() -> {
            UserProfile created = new UserProfile();
            created.setUserId(userId);
            return created;
        });
        profile.setGender(clean(request.getGender()));
        profile.setBirthday(request.getBirthday());
        profile.setLearningDirection(clean(request.getLearningDirection()));
        profile.setCity(clean(request.getCity()));
        profile.setBio(clean(request.getBio()));
        profileRepository.save(profile);

        auditService.audit(userId, "UPDATE_PROFILE", String.valueOf(userId), "SUCCESS", ip, userAgent, "nickname=" + user.getNickname());
        log.info("profile update success userId={}", userId);
        return userAssembler.toVO(user);
    }

    private String clean(String value) {
        if (value == null) {
            return null;
        }
        return value.trim().replaceAll("<[^>]*>", "");
    }
}
