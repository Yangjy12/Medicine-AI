package com.xinglin.user.service;

import com.xinglin.user.entity.AppUser;
import com.xinglin.user.entity.UserAccount;
import com.xinglin.user.entity.UserProfile;
import com.xinglin.user.repository.UserAccountRepository;
import com.xinglin.user.repository.UserProfileRepository;
import com.xinglin.user.vo.UserVO;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class UserAssembler {
    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final LevelService levelService;

    public UserAssembler(UserAccountRepository accountRepository,
                         UserProfileRepository profileRepository,
                         LevelService levelService) {
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.levelService = levelService;
    }

    public UserVO toVO(AppUser user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setUsername(user.getUsername());
        vo.setPhoneMasked(maskPhone(user.getPhone()));
        vo.setNickname(user.getNickname());
        vo.setAvatar(user.getAvatar());
        vo.setLevel(user.getLevel());
        vo.setLevelName(levelService.nameOf(user.getLevel()));
        vo.setStatus(user.getStatus());
        vo.setRoles(Collections.singletonList("USER"));
        accountRepository.findByUserId(user.getId()).ifPresent(account -> {
            vo.setAvailablePoints(account.getAvailablePoints());
            vo.setTotalPoints(account.getTotalPoints());
        });
        profileRepository.findByUserId(user.getId()).ifPresent(profile -> fillProfile(vo, profile));
        return vo;
    }

    private void fillProfile(UserVO vo, UserProfile profile) {
        vo.setGender(profile.getGender());
        vo.setBirthday(profile.getBirthday());
        vo.setLearningDirection(profile.getLearningDirection());
        vo.setCity(profile.getCity());
        vo.setBio(profile.getBio());
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
