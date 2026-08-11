package com.xinglin.user.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "level_rule", uniqueConstraints = @UniqueConstraint(name = "uk_level_rule_level", columnNames = "level"))
public class LevelRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Integer level;
    @Column(name = "level_name", nullable = false, length = 64)
    private String levelName;
    @Column(name = "min_total_points", nullable = false)
    private Long minTotalPoints = 0L;
    @Column(nullable = false)
    private Integer enabled = 1;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PreUpdate
    public void preUpdate() { this.updatedAt = LocalDateTime.now(); }

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
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
