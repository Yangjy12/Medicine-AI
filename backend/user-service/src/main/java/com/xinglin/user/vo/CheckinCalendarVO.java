package com.xinglin.user.vo;

import java.util.ArrayList;
import java.util.List;

public class CheckinCalendarVO {
    private String month;
    private List<Integer> checkedDays = new ArrayList<>();
    private Integer streakDays;

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public List<Integer> getCheckedDays() { return checkedDays; }
    public void setCheckedDays(List<Integer> checkedDays) { this.checkedDays = checkedDays; }
    public Integer getStreakDays() { return streakDays; }
    public void setStreakDays(Integer streakDays) { this.streakDays = streakDays; }
}
