package com.xinglin.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class RefreshTokenRequest {
    @NotBlank
    private String refreshToken;

    @Size(max = 128)
    private String deviceId = "web";

    public String getRefreshToken() { return refreshToken; }
    public void setRefreshToken(String refreshToken) { this.refreshToken = refreshToken; }
    public String getDeviceId() { return deviceId; }
    public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
}
