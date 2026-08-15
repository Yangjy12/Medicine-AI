package com.xinglin.user.service;

import com.xinglin.user.common.BusinessException;
import com.xinglin.user.common.PageResponse;
import com.xinglin.user.entity.AppUser;
import com.xinglin.user.entity.PointsRecord;
import com.xinglin.user.entity.UserAccount;
import com.xinglin.user.repository.AppUserRepository;
import com.xinglin.user.repository.PointsRecordRepository;
import com.xinglin.user.repository.UserAccountRepository;
import com.xinglin.user.vo.PointsAccountVO;
import com.xinglin.user.vo.PointsRecordVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class PointsService {
    private static final Logger log = LoggerFactory.getLogger(PointsService.class);
    private final UserAccountRepository accountRepository;
    private final PointsRecordRepository pointsRecordRepository;
    private final AppUserRepository userRepository;
    private final StringRedisTemplate redisTemplate;
    private final LevelService levelService;

    public PointsService(UserAccountRepository accountRepository,
                         PointsRecordRepository pointsRecordRepository,
                         AppUserRepository userRepository,
                         StringRedisTemplate redisTemplate,
                         LevelService levelService) {
        this.accountRepository = accountRepository;
        this.pointsRecordRepository = pointsRecordRepository;
        this.userRepository = userRepository;
        this.redisTemplate = redisTemplate;
        this.levelService = levelService;
    }

    @Transactional
    public boolean addPoints(Long userId, String bizType, String bizId, int points, String description) {
        if (points == 0) {
            throw new BusinessException(400, "积分变更不能为0");
        }
        String idempotentKey = "user:points:idempotent:" + bizType + ":" + bizId + ":" + userId;
        Boolean first = redisTemplate.opsForValue().setIfAbsent(idempotentKey, "1", Duration.ofDays(7));
        if (Boolean.FALSE.equals(first)) {
            log.warn("points idempotent ignored userId={} bizType={} bizId={}", userId, bizType, bizId);
            return false;
        }
        if (pointsRecordRepository.existsByUserIdAndBizTypeAndBizId(userId, bizType, bizId)) {
            log.warn("points db idempotent ignored userId={} bizType={} bizId={}", userId, bizType, bizId);
            return false;
        }
        try {
            PointsRecord record = new PointsRecord();
            record.setUserId(userId);
            record.setBizType(bizType);
            record.setBizId(bizId);
            record.setPoints(points);
            record.setOperationType(points >= 0 ? "ADD" : "DEDUCT");
            record.setDescription(description);
            pointsRecordRepository.save(record);

            int totalDelta = Math.max(points, 0);
            for (int i = 1; i <= 3; i++) {
                UserAccount account = accountRepository.findByUserId(userId)
                        .orElseThrow(() -> new BusinessException(404, "积分账户不存在"));
                if (points < 0 && account.getAvailablePoints() + points < 0) {
                    throw new BusinessException(400, "可用积分不足");
                }
                int updated = accountRepository.changePointsCas(userId, points, totalDelta, account.getVersion());
                if (updated == 1) {
                    updateUserLevel(userId);
                    log.info("points add success userId={} bizType={} bizId={} points={} retry={}", userId, bizType, bizId, points, i - 1);
                    return true;
                }
                log.warn("points cas conflict userId={} bizType={} bizId={} retry={}", userId, bizType, bizId, i);
            }
            throw new BusinessException(500, "积分账户更新冲突，请稍后重试");
        } catch (DataIntegrityViolationException ex) {
            log.warn("points unique idempotent ignored userId={} bizType={} bizId={}", userId, bizType, bizId);
            return false;
        } catch (RuntimeException ex) {
            redisTemplate.delete(idempotentKey);
            log.warn("points change failed and idempotent key released userId={} bizType={} bizId={} error={}",
                    userId, bizType, bizId, ex.getMessage());
            throw ex;
        }
    }

    public PointsAccountVO account(Long userId) {
        UserAccount account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(404, "积分账户不存在"));
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        PointsAccountVO vo = new PointsAccountVO();
        vo.setAvailablePoints(account.getAvailablePoints());
        vo.setTotalPoints(account.getTotalPoints());
        vo.setLevel(user.getLevel());
        vo.setLevelName(levelService.nameOf(user.getLevel()));
        vo.setNextLevelPoints(levelService.nextLevelPoints(user.getLevel()));
        return vo;
    }

    public PageResponse<PointsRecordVO> records(Long userId, int page, int pageSize) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.min(Math.max(pageSize, 1), 50);
        Page<PointsRecord> result = pointsRecordRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(safePage - 1, safeSize));
        List<PointsRecordVO> records = result.getContent().stream().map(this::toVO).collect(Collectors.toList());
        return new PageResponse<>(records, safePage, safeSize, result.getTotalElements());
    }

    private void updateUserLevel(Long userId) {
        UserAccount account = accountRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(404, "积分账户不存在"));
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        int level = levelService.levelOf(account.getTotalPoints());
        if (!user.getLevel().equals(level)) {
            user.setLevel(level);
            userRepository.save(user);
            log.info("user level changed userId={} level={}", userId, level);
        }
    }

    private PointsRecordVO toVO(PointsRecord record) {
        PointsRecordVO vo = new PointsRecordVO();
        vo.setId(record.getId());
        vo.setBizType(record.getBizType());
        vo.setBizId(record.getBizId());
        vo.setPoints(record.getPoints());
        vo.setOperationType(record.getOperationType());
        vo.setDescription(record.getDescription());
        vo.setCreatedAt(record.getCreatedAt());
        return vo;
    }
}
