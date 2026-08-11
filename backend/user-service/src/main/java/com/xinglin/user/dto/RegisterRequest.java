package com.xinglin.user.dto;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegisterRequest {
    @NotBlank
    @Size(min = 4, max = 32)
    @Pattern(regexp = "^[\\u4e00-\\u9fa5A-Za-z0-9_]+$")
    private String username;

    @Pattern(regexp = "^$|^1[3-9]\\d{9}$")
    private String phone;

    @NotBlank
    @Size(min = 8, max = 32)
    private String password;

    @NotBlank
    @Size(min = 2, max = 20)
    private String nickname;

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
}
