package com.xinglin.user.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

public class SaveLevelRuleRequest {
    private Long id;
    @NotNull
    @Min(1)
    private Integer level;
    @NotBlank
    @Size(max = 64)
    private String levelName;
    @NotNull
    @Min(0)
    private Long minTotalPoints;
    @Min(0)
    @Max(1)
    private Integer enabled = 1;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getLevel() { return level; }
    public void setLevel(Integer level) { this.level = level; }
    public String getLevelName() { return levelName; }
    public void setLevelName(String levelName) { this.levelName = levelName; }
    public Long getMinTotalPoints() { return minTotalPoints; }
    public void setMinTotalPoints(Long minTotalPoints) { this.minTotalPoints = minTotalPoints; }
    public Integer getEnabled() { return enabled; }
    public void setEnabled(Integer enabled) { this.enabled = enabled; }
}
