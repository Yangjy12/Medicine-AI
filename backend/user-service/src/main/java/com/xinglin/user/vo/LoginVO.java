package com.xinglin.user.vo;

public class LoginVO {
    private String accessToken;
    private String refreshToken;
    private Long expiresIn;
    private UserVO user;

    public String getAccessToken() { return accessToken; }
    public void setAccessToken(String accessToken) { this.accessToken = accessToken; }
    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public Long getExpiresIn() { return expiresIn; }
    public void setExpiresIn(Long expiresIn) { this.expiresIn = expiresIn; }
    public UserVO getUser() { return user; }
    public void setUser(UserVO user) { this.user = user; }
}
