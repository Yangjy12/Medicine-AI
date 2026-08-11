package com.xinglin.user.dto;

import javax.validation.constraints.Size;
import java.time.LocalDate;

public class ProfileUpdateRequest {
    @Size(min = 2, max = 20)
    private String nickname;
    @Size(max = 512)
    private String avatar;
    @Size(max = 16)
    private String gender;
    private LocalDate birthday;
    @Size(max = 128)
    private String learningDirection;
    @Size(max = 64)
    private String city;
    @Size(max = 512)
    private String bio;

    public String getNickname() { return nickname; }
    public void setNickname(String nickname) { this.nickname = nickname; }
    public String getAvatar() { return avatar; }
    public void setAvatar(String avatar) { this.avatar = avatar; }
    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }
    public LocalDate getBirthday() { return birthday; }
    public void setBirthday(LocalDate birthday) { this.birthday = birthday; }
    public String getLearningDirection() { return learningDirection; }
    public void setLearningDirection(String learningDirection) { this.learningDirection = learningDirection; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
    public String getBio() { return bio; }
    public void setBio(String bio) { this.bio = bio; }
}
