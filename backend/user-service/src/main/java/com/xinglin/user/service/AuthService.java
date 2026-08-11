package com.xinglin.user.service;

import com.xinglin.user.common.BusinessException;
import com.xinglin.user.dto.LoginRequest;
import com.xinglin.user.dto.RefreshTokenRequest;
import com.xinglin.user.dto.RegisterRequest;
import com.xinglin.user.entity.AppUser;
import com.xinglin.user.entity.UserAccount;
import com.xinglin.user.entity.UserProfile;
import com.xinglin.user.repository.AppUserRepository;
import com.xinglin.user.repository.UserAccountRepository;
import com.xinglin.user.repository.UserProfileRepository;
import com.xinglin.user.security.JwtService;
import com.xinglin.user.security.MessageDigestUtil;
import com.xinglin.user.vo.LoginVO;
import com.xinglin.user.vo.TokenVO;
import com.xinglin.user.vo.UserVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class AuthService {
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final AppUserRepository userRepository;
    private final UserAccountRepository accountRepository;
    private final UserProfileRepository profileRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserAssembler userAssembler;
    private final StringRedisTemplate redisTemplate;
    private final AuditService auditService;
    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${xinglin.auth.refresh-token-ttl-days:7}")
    private long refreshTokenTtlDays;

    @Value("${xinglin.security.login-max-failures:5}")
    private int loginMaxFailures;

    @Value("${xinglin.security.login-lock-minutes:15}")
    private int loginLockMinutes;

    public AuthService(AppUserRepository userRepository,
                       UserAccountRepository accountRepository,
                       UserProfileRepository profileRepository,
                       BCryptPasswordEncoder passwordEncoder,
                       JwtService jwtService,
                       UserAssembler userAssembler,
                       StringRedisTemplate redisTemplate,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.profileRepository = profileRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userAssembler = userAssembler;
        this.redisTemplate = redisTemplate;
        this.auditService = auditService;
    }

    @Transactional
    public UserVO register(RegisterRequest request, String ip, String userAgent) {
        validatePassword(request.getPassword(), request.getUsername(), request.getPhone());
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(409, "用户名已存在");
        }
        if (StringUtils.hasText(request.getPhone()) && userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(409, "手机号已注册");
        }

        AppUser user = new AppUser();
        user.setUsername(request.getUsername().trim());
        user.setPhone(StringUtils.hasText(request.getPhone()) ? request.getPhone().trim() : null);
        user.setNickname(stripHtml(request.getNickname().trim()));
        user.setAvatar("/assets/avatar/default.png");
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        AppUser saved = userRepository.save(user);

        UserAccount account = new UserAccount();
        account.setUserId(saved.getId());
        accountRepository.save(account);

        UserProfile profile = new UserProfile();
        profile.setUserId(saved.getId());
        profileRepository.save(profile);

        auditService.audit(saved.getId(), "REGISTER", String.valueOf(saved.getId()), "SUCCESS", ip, userAgent, "username=" + saved.getUsername());
        log.info("register success userId={} username={} phoneMasked={}", saved.getId(), saved.getUsername(), maskPhone(saved.getPhone()));
        return userAssembler.toVO(saved);
    }

    @Transactional
    public LoginVO login(LoginRequest request, String ip, String userAgent) {
        String account = request.getAccount().trim();
        String lockKey = "user:login:lock:account:" + account;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(lockKey))) {
            auditService.login(null, account, "LOCKED", "账号临时锁定", ip, userAgent, request.getDeviceId());
            throw new BusinessException(423, "账号临时锁定，请稍后再试");
        }

        AppUser user = findByAccount(account);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            long failCount = redisTemplate.opsForValue().increment("user:login:fail:account:" + account);
            redisTemplate.expire("user:login:fail:account:" + account, Duration.ofMinutes(5));
            if (failCount >= loginMaxFailures) {
                redisTemplate.opsForValue().set(lockKey, "1", Duration.ofMinutes(loginLockMinutes));
            }
            auditService.login(user == null ? null : user.getId(), account, "FAIL", "账号或密码错误", ip, userAgent, request.getDeviceId());
            log.warn("login failed account={} ip={} failCount={}", account, ip, failCount);
            throw new BusinessException(401, "账号或密码错误");
        }
        if (!"NORMAL".equals(user.getStatus())) {
            auditService.login(user.getId(), account, "DISABLED", "账号状态异常", ip, userAgent, request.getDeviceId());
            throw new BusinessException(403, "账号状态异常");
        }

        redisTemplate.delete("user:login:fail:account:" + account);
        user.setLastLoginTime(LocalDateTime.now());
        userRepository.save(user);

        String accessToken = jwtService.createAccessToken(user);
        String refreshToken = createRefreshToken(user.getId());
        storeRefreshToken(user.getId(), normalizeDevice(request.getDeviceId()), refreshToken);

        LoginVO vo = new LoginVO();
        vo.setAccessToken(accessToken);
        vo.setRefreshToken(refreshToken);
        vo.setExpiresIn(jwtService.getAccessTokenTtlSeconds());
        vo.setUser(userAssembler.toVO(user));
        auditService.login(user.getId(), account, "SUCCESS", null, ip, userAgent, request.getDeviceId());
        log.info("login success userId={} account={} ip={} deviceId={}", user.getId(), account, ip, request.getDeviceId());
        return vo;
    }

    public TokenVO refresh(RefreshTokenRequest request) {
        Long userId = parseRefreshUserId(request.getRefreshToken());
        AppUser user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(401, "刷新令牌无效"));
        String key = refreshKey(userId, normalizeDevice(request.getDeviceId()));
        String storedHash = redisTemplate.opsForValue().get(key);
        if (!StringUtils.hasText(storedHash) || !MessageDigestUtil.constantTimeEquals(storedHash, MessageDigestUtil.sha256(request.getRefreshToken()))) {
            throw new BusinessException(401, "刷新令牌无效或已过期");
        }
        if (!"NORMAL".equals(user.getStatus())) {
            throw new BusinessException(403, "账号状态异常");
        }
        log.info("refresh token success userId={} deviceId={}", userId, request.getDeviceId());
        return new TokenVO(jwtService.createAccessToken(user), jwtService.getAccessTokenTtlSeconds());
    }

    public void logout(Long userId, String deviceId, String ip, String userAgent) {
        redisTemplate.delete(refreshKey(userId, normalizeDevice(deviceId)));
        auditService.audit(userId, "LOGOUT", String.valueOf(userId), "SUCCESS", ip, userAgent, "deviceId=" + normalizeDevice(deviceId));
        log.info("logout success userId={} deviceId={}", userId, deviceId);
    }

    public UserVO me(Long userId) {
        AppUser user = userRepository.findById(userId).orElseThrow(() -> new BusinessException(404, "用户不存在"));
        return userAssembler.toVO(user);
    }

    private AppUser findByAccount(String account) {
        return userRepository.findByUsername(account)
                .orElseGet(() -> userRepository.findByPhone(account).orElse(null));
    }

    private void validatePassword(String password, String username, String phone) {
        boolean hasLetter = password.chars().anyMatch(Character::isLetter);
        boolean hasDigit = password.chars().anyMatch(Character::isDigit);
        if (!hasLetter || !hasDigit) {
            throw new BusinessException(400, "密码至少包含字母和数字");
        }
        String lower = password.toLowerCase();
        if (lower.contains(username.toLowerCase()) || (StringUtils.hasText(phone) && lower.contains(phone.substring(phone.length() - 4)))) {
            throw new BusinessException(400, "密码不能包含用户名或手机号后四位");
        }
    }

    private String createRefreshToken(Long userId) {
        byte[] random = new byte[32];
        secureRandom.nextBytes(random);
        return userId + "." + Base64.getUrlEncoder().withoutPadding().encodeToString(random);
    }

    private Long parseRefreshUserId(String refreshToken) {
        try {
            return Long.valueOf(refreshToken.substring(0, refreshToken.indexOf('.')));
        } catch (Exception ex) {
            throw new BusinessException(401, "刷新令牌格式无效");
        }
    }

    private void storeRefreshToken(Long userId, String deviceId, String refreshToken) {
        redisTemplate.opsForValue().set(refreshKey(userId, deviceId), MessageDigestUtil.sha256(refreshToken), Duration.ofDays(refreshTokenTtlDays));
    }

    private String refreshKey(Long userId, String deviceId) {
        return "user:refresh:" + userId + ":" + deviceId;
    }

    private String normalizeDevice(String deviceId) {
        return StringUtils.hasText(deviceId) ? deviceId.trim() : "web";
    }

    private String stripHtml(String value) {
        return value == null ? null : value.replaceAll("<[^>]*>", "");
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() != 11) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(7);
    }
}
