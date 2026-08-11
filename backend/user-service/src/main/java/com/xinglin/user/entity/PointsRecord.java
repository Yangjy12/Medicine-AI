package com.xinglin.user.entity;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "points_record",
        uniqueConstraints = @UniqueConstraint(name = "uk_points_biz_user", columnNames = {"biz_type", "biz_id", "user_id"}),
        indexes = {
                @Index(name = "idx_points_user_time", columnList = "user_id,created_at"),
                @Index(name = "idx_points_biz", columnList = "biz_type,biz_id")
        })
public class PointsRecord {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(name = "user_id", nullable = false)
    private Long userId;
    @Column(name = "biz_type", nullable = false, length = 64)
    private String bizType;
    @Column(name = "biz_id", nullable = false, length = 128)
    private String bizId;
    @Column(nullable = false)
    private Integer points;
    @Column(name = "operation_type", nullable = false, length = 32)
    private String operationType;
    @Column(length = 255)
    private String description;
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getBizType() { return bizType; }
    public void setBizType(String bizType) { this.bizType = bizType; }
    public String getBizId() { return bizId; }
    public void setBizId(String bizId) { this.bizId = bizId; }
    public Integer getPoints() { return points; }
    public void setPoints(Integer points) { this.points = points; }
    public String getOperationType() { return operationType; }
    public void setOperationType(String operationType) { this.operationType = operationType; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
