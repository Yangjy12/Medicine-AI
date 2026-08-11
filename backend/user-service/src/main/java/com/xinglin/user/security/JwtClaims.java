package com.xinglin.user.security;

import java.util.ArrayList;
import java.util.List;

public class JwtClaims {
    private Long userId;
    private String username;
    private List<String> roles = new ArrayList<>();
    private Integer tokenVersion;
    private Long expiresAt;
    private String jwtId;

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public List<String> getRoles() { return roles; }
    public void setRoles(List<String> roles) { this.roles = roles; }
    public Integer getTokenVersion() { return tokenVersion; }
    public void setTokenVersion(Integer tokenVersion) { this.tokenVersion = tokenVersion; }
    public Long getExpiresAt() { return expiresAt; }
    public void setExpiresAt(Long expiresAt) { this.expiresAt = expiresAt; }
    public String getJwtId() { return jwtId; }
    public void setJwtId(String jwtId) { this.jwtId = jwtId; }
}
