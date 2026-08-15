package com.xinglin.chat.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinglin.chat.common.BusinessException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;

@Service
public class ChatJwtService {
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;
    private final String secret;

    public ChatJwtService(ObjectMapper objectMapper,
                          StringRedisTemplate redisTemplate,
                          @Value("${xinglin.auth.jwt-secret}") String secret) {
        this.objectMapper = objectMapper;
        this.redisTemplate = redisTemplate;
        this.secret = secret;
    }

    public AuthenticatedUser parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(401, "Token格式不合法");
            }
            String signingInput = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(signingInput), parts[2])) {
                throw new BusinessException(401, "Token签名无效");
            }
            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {});
            long exp = ((Number) payload.get("exp")).longValue();
            if (exp < Instant.now().getEpochSecond()) {
                throw new BusinessException(401, "登录状态已过期");
            }
            assertNotBlacklisted(payload);
            AuthenticatedUser user = new AuthenticatedUser();
            user.setUserId(Long.valueOf(String.valueOf(payload.get("sub"))));
            user.setUsername(String.valueOf(payload.get("username")));
            Object roles = payload.get("roles");
            List<String> values = new ArrayList<>();
            if (roles instanceof List) {
                for (Object role : (List<?>) roles) {
                    values.add(String.valueOf(role));
                }
            }
            user.setRoles(values);
            return user;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(401, "Token解析失败");
        }
    }

    private String sign(String input) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(mac.doFinal(input.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT签名失败", ex);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(a.getBytes(StandardCharsets.UTF_8), b.getBytes(StandardCharsets.UTF_8));
    }

    private void assertNotBlacklisted(Map<String, Object> payload) {
        Object jtiValue = payload.get("jti");
        String jwtId = jtiValue == null ? null : String.valueOf(jtiValue);
        if (StringUtils.hasText(jwtId) && Boolean.TRUE.equals(redisTemplate.hasKey(accessBlacklistKey(jwtId)))) {
            throw new BusinessException(401, "登录状态已失效，请重新登录");
        }
    }

    private String accessBlacklistKey(String jwtId) {
        return "user:access:blacklist:" + jwtId;
    }
}
