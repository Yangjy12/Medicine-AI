package com.xinglin.user.service;

import com.xinglin.user.common.BusinessException;
import com.xinglin.user.entity.CheckinRecord;
import com.xinglin.user.repository.CheckinRecordRepository;
import com.xinglin.user.repository.UserAccountRepository;
import com.xinglin.user.vo.CheckinCalendarVO;
import com.xinglin.user.vo.CheckinVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class CheckinService {
    private static final Logger log = LoggerFactory.getLogger(CheckinService.class);
    private static final DateTimeFormatter MONTH_FORMAT = DateTimeFormatter.ofPattern("yyyyMM");
    private final StringRedisTemplate redisTemplate;
    private final CheckinRecordRepository checkinRecordRepository;
    private final UserAccountRepository accountRepository;
    private final PointsService pointsService;
    private final PointsRuleService pointsRuleService;
    private final AuditService auditService;

    public CheckinService(StringRedisTemplate redisTemplate,
                          CheckinRecordRepository checkinRecordRepository,
                          UserAccountRepository accountRepository,
                          PointsService pointsService,
                          PointsRuleService pointsRuleService,
                          AuditService auditService) {
        this.redisTemplate = redisTemplate;
        this.checkinRecordRepository = checkinRecordRepository;
        this.accountRepository = accountRepository;
        this.pointsService = pointsService;
        this.pointsRuleService = pointsRuleService;
        this.auditService = auditService;
    }

    @Transactional
    public CheckinVO checkin(Long userId, String ip, String userAgent) {
        accountRepository.findByUserId(userId).orElseThrow(() -> new BusinessException(404, "积分账户不存在"));
        LocalDate today = LocalDate.now();
        String key = bitmapKey(userId, YearMonth.from(today));
        int offset = today.getDayOfMonth() - 1;
        Boolean old = redisTemplate.opsForValue().setBit(key, offset, true);
        if (Boolean.TRUE.equals(old)) {
            if (checkinRecordRepository.existsByUserIdAndCheckinDate(userId, today)) {
                CheckinVO vo = buildVO(true, true, 0, streakDays(userId, today), checkinRecordRepository.countByUserId(userId));
                log.info("checkin repeated userId={} date={}", userId, today);
                return vo;
            }
            redisTemplate.opsForValue().setBit(key, offset, false);
            old = redisTemplate.opsForValue().setBit(key, offset, true);
            log.warn("checkin bitmap inconsistent repaired userId={} date={} previousOld={}", userId, today, old);
        }
        try {
            if (checkinRecordRepository.existsByUserIdAndCheckinDate(userId, today)) {
                CheckinVO vo = buildVO(true, true, 0, streakDays(userId, today), checkinRecordRepository.countByUserId(userId));
                log.info("checkin record repeated userId={} date={}", userId, today);
                return vo;
            }
            int streak = streakDays(userId, today);
            int reward = pointsRuleService.requireEnabledRule("CHECKIN").getPoints();
            if (streak > 0 && streak % 7 == 0) {
                reward += pointsRuleService.requireEnabledRule("CHECKIN_7_DAYS").getPoints();
            }
            CheckinRecord record = new CheckinRecord();
            record.setUserId(userId);
            record.setCheckinDate(today);
            record.setRewardPoints(reward);
            record.setStreakDays(streak);
            checkinRecordRepository.save(record);
            pointsService.addPoints(userId, "CHECKIN", today.toString(), reward, "每日签到");
            auditService.audit(userId, "CHECKIN", today.toString(), "SUCCESS", ip, userAgent, "rewardPoints=" + reward + ",streakDays=" + streak);
            log.info("checkin success userId={} date={} rewardPoints={} streakDays={}", userId, today, reward, streak);
            return buildVO(true, false, reward, streak, checkinRecordRepository.countByUserId(userId));
        } catch (DataIntegrityViolationException ex) {
            CheckinVO vo = buildVO(true, true, 0, streakDays(userId, today), checkinRecordRepository.countByUserId(userId));
            log.info("checkin unique repeated userId={} date={}", userId, today);
            return vo;
        } catch (RuntimeException ex) {
            redisTemplate.opsForValue().setBit(key, offset, false);
            log.warn("checkin failed and bitmap rolled back userId={} date={} error={}", userId, today, ex.getMessage());
            throw ex;
        }
    }

    public CheckinCalendarVO calendar(Long userId, String month) {
        YearMonth yearMonth = month == null || month.trim().isEmpty()
                ? YearMonth.now()
                : YearMonth.parse(month.trim(), DateTimeFormatter.ofPattern("yyyy-MM"));
        CheckinCalendarVO vo = new CheckinCalendarVO();
        vo.setMonth(yearMonth.toString());
        String key = bitmapKey(userId, yearMonth);
        refillMonthBitmapFromDb(userId, yearMonth, key);
        for (int day = 1; day <= yearMonth.lengthOfMonth(); day++) {
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, day - 1))) {
                vo.getCheckedDays().add(day);
            }
        }
        vo.setStreakDays(streakDays(userId, LocalDate.now()));
        return vo;
    }

    private CheckinVO buildVO(boolean checked, boolean alreadyChecked, int rewardPoints, int streakDays, long totalCheckedDays) {
        CheckinVO vo = new CheckinVO();
        vo.setChecked(checked);
        vo.setAlreadyChecked(alreadyChecked);
        vo.setRewardPoints(rewardPoints);
        vo.setStreakDays(streakDays);
        vo.setTotalCheckedDays(totalCheckedDays);
        return vo;
    }

    private int streakDays(Long userId, LocalDate from) {
        int streak = 0;
        LocalDate cursor = from;
        for (int i = 0; i < 370; i++) {
            YearMonth month = YearMonth.from(cursor);
            String key = bitmapKey(userId, month);
            if (Boolean.TRUE.equals(redisTemplate.opsForValue().getBit(key, cursor.getDayOfMonth() - 1))) {
                streak++;
                cursor = cursor.minusDays(1);
            } else if (checkinRecordRepository.existsByUserIdAndCheckinDate(userId, cursor)) {
                redisTemplate.opsForValue().setBit(key, cursor.getDayOfMonth() - 1, true);
                streak++;
                cursor = cursor.minusDays(1);
            } else {
                break;
            }
        }
        return streak;
    }

    private void refillMonthBitmapFromDb(Long userId, YearMonth month, String key) {
        LocalDate start = month.atDay(1);
        LocalDate end = month.atEndOfMonth();
        List<CheckinRecord> records = checkinRecordRepository.findByUserIdAndCheckinDateBetweenOrderByCheckinDateAsc(userId, start, end);
        for (CheckinRecord record : records) {
            redisTemplate.opsForValue().setBit(key, record.getCheckinDate().getDayOfMonth() - 1, true);
        }
        if (!records.isEmpty()) {
            log.info("checkin bitmap refilled userId={} month={} count={}", userId, month, records.size());
        }
    }

    private String bitmapKey(Long userId, YearMonth month) {
        return "user:checkin:" + userId + ":" + month.format(MONTH_FORMAT);
    }
}
