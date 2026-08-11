package com.xinglin.user.vo;

public class PointsAccountVO {
    private Long availablePoints;
    private Long totalPoints;
    private Integer level;
    private String levelName;
    private Long nextLevelPoints;

    public Long getAvailablePoints() { return availablePoints; }
    public void setAvailablePoints(Long availablePoints) { this.availablePoints = availablePoints; }
    public Long getTotalPoints() { return totalPoints; }
    public void setTotalPoints(Long totalPoints) { this.totalPoints = totalPoints; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public Long getNextLevelPoints() { return nextLevelPoints; }
    public void setNextLevelPoints(Long nextLevelPoints) { this.nextLevelPoints = nextLevelPoints; }
}
