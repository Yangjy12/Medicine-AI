package com.xinglin.user.security;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xinglin.user.common.BusinessException;
import com.xinglin.user.entity.AppUser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

@Service
public class JwtService {
    private final ObjectMapper objectMapper;
    private final String secret;
    private final long accessTokenTtlSeconds;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${xinglin.auth.jwt-secret}") String secret,
                      @Value("${xinglin.auth.access-token-ttl-seconds:7200}") long accessTokenTtlSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.accessTokenTtlSeconds = accessTokenTtlSeconds;
    }

    public String createAccessToken(AppUser user) {
        long now = Instant.now().getEpochSecond();
        long exp = now + accessTokenTtlSeconds;
        Map<String, Object> header = new LinkedHashMap<>();
        header.put("alg", "HS256");
        header.put("typ", "JWT");

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", String.valueOf(user.getId()));
        payload.put("username", user.getUsername());
        payload.put("roles", Collections.singletonList("USER"));
        payload.put("tokenVersion", user.getTokenVersion());
        payload.put("iat", now);
        payload.put("exp", exp);
        payload.put("jti", UUID.randomUUID().toString());

        String encodedHeader = base64Json(header);
        String encodedPayload = base64Json(payload);
        String signingInput = encodedHeader + "." + encodedPayload;
        return signingInput + "." + sign(signingInput);
    }

    public JwtClaims parseAndValidate(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new BusinessException(401, "Token格式不合法");
            }
            String signingInput = parts[0] + "." + parts[1];
            String expected = sign(signingInput);
            if (!constantTimeEquals(expected, parts[2])) {
                throw new BusinessException(401, "Token签名无效");
            }
            Map<String, Object> payload = objectMapper.readValue(
                    Base64.getUrlDecoder().decode(parts[1]),
                    new TypeReference<Map<String, Object>>() {});
            long exp = ((Number) payload.get("exp")).longValue();
            if (exp < Instant.now().getEpochSecond()) {
                throw new BusinessException(401, "登录状态已过期");
            }
            JwtClaims claims = new JwtClaims();
            claims.setUserId(Long.valueOf(String.valueOf(payload.get("sub"))));
            claims.setUsername(String.valueOf(payload.get("username")));
            claims.setTokenVersion(((Number) payload.get("tokenVersion")).intValue());
            claims.setExpiresAt(exp);
            claims.setJwtId(String.valueOf(payload.get("jti")));
            Object roles = payload.get("roles");
            if (roles instanceof List) {
                List<String> values = new ArrayList<>();
                for (Object role : (List<?>) roles) {
                    values.add(String.valueOf(role));
                }
                claims.setRoles(values);
            }
            return claims;
        } catch (BusinessException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new BusinessException(401, "Token解析失败");
        }
    }

    public long getAccessTokenTtlSeconds() {
        return accessTokenTtlSeconds;
    }

    private String base64Json(Map<String, Object> value) {
        try {
            return Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception ex) {
            throw new IllegalStateException("JWT序列化失败", ex);
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
        return MessageDigestUtil.constantTimeEquals(a, b);
    }
}
