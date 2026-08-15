package com.xinglin.chat.service;

import com.xinglin.chat.common.BusinessException;
import com.xinglin.chat.entity.AppUserSummary;
import com.xinglin.chat.repository.AppUserSummaryRepository;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class UserDirectoryService {
    private static final String NORMAL = "NORMAL";

    private final AppUserSummaryRepository userRepository;

    public UserDirectoryService(AppUserSummaryRepository userRepository) {
        this.userRepository = userRepository;
    }

    public void requireNormalUser(Long userId) {
        if (userId == null || userId <= 0 || !userRepository.existsByIdAndStatus(userId, NORMAL)) {
            throw new BusinessException(404, "用户不存在或已禁用");
        }
    }

    public void requireNormalUsers(Collection<Long> userIds) {
        Set<Long> normalized = normalizeIds(userIds);
        if (normalized.isEmpty()) {
            return;
        }
        Map<Long, AppUserSummary> users = findNormalUsers(normalized);
        if (users.size() != normalized.size()) {
            throw new BusinessException(404, "包含不存在或已禁用的用户");
        }
    }

    public Map<Long, AppUserSummary> findNormalUsers(Collection<Long> userIds) {
        Set<Long> normalized = normalizeIds(userIds);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return userRepository.findByIdInAndStatus(normalized, NORMAL)
                .stream()
                .collect(Collectors.toMap(AppUserSummary::getId, Function.identity(), (left, right) -> left));
    }

    public String displayName(Long userId) {
        if (userId == null) {
            return "用户";
        }
        return findNormalUsers(Collections.singleton(userId))
                .values()
                .stream()
                .findFirst()
                .map(this::displayName)
                .orElse("用户 " + userId);
    }

    public String avatar(Long userId) {
        if (userId == null) {
            return null;
        }
        return findNormalUsers(Collections.singleton(userId))
                .values()
                .stream()
                .findFirst()
                .map(AppUserSummary::getAvatar)
                .orElse(null);
    }

    public String displayName(AppUserSummary user) {
        if (user == null) {
            return "用户";
        }
        if (StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        if (StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return "用户 " + user.getId();
    }

    private Set<Long> normalizeIds(Collection<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Collections.emptySet();
        }
        return userIds.stream()
                .filter(id -> id != null && id > 0)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }
}
