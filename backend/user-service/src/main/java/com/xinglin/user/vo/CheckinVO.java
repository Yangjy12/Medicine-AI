package com.xinglin.user.vo;

public class CheckinVO {
    private Boolean checked;
    private Boolean alreadyChecked;
    private Integer rewardPoints;
    private Integer streakDays;
    private Long totalCheckedDays;

    public Boolean getChecked() { return checked; }
    public void setChecked(Boolean checked) { this.checked = checked; }
    public Boolean getAlreadyChecked() { return alreadyChecked; }
    public void setAlreadyChecked(Boolean alreadyChecked) { this.alreadyChecked = alreadyChecked; }
    public Integer getRewardPoints() { return rewardPoints; }
    public void setRewardPoints(Integer rewardPoints) { this.rewardPoints = rewardPoints; }
    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
    public Long getTotalCheckedDays() { return totalCheckedDays; }
    public void setTotalCheckedDays(Long totalCheckedDays) { this.totalCheckedDays = totalCheckedDays; }
}
