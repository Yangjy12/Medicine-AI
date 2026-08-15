package com.xinglin.forum.service;

import com.xinglin.forum.entity.AppUserSummary;
import com.xinglin.forum.repository.AppUserSummaryRepository;
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

    public Map<Long, AppUserSummary> findNormalUsers(Collection<Long> userIds) {
        Set<Long> normalized = normalizeIds(userIds);
        if (normalized.isEmpty()) {
            return Collections.emptyMap();
        }
        return userRepository.findByIdInAndStatus(normalized, NORMAL)
                .stream()
                .collect(Collectors.toMap(AppUserSummary::getId, Function.identity(), (left, right) -> left));
    }

    public String displayName(AppUserSummary user, Long fallbackUserId) {
        if (user != null && StringUtils.hasText(user.getNickname())) {
            return user.getNickname();
        }
        if (user != null && StringUtils.hasText(user.getUsername())) {
            return user.getUsername();
        }
        return fallbackUserId == null ? "用户" : "用户 " + fallbackUserId;
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
