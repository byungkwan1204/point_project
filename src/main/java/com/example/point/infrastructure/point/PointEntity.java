package com.example.point.infrastructure.point;

import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointRewardType;
import com.example.point.domain.model.PointStatus;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "points")
@EntityListeners(AuditingEntityListener.class)
public class PointEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long pointKey;

    private Long userKey;

    @Enumerated(EnumType.STRING)
    private PointStatus status;

    @Enumerated(EnumType.STRING)
    private PointRewardType rewardType;

    private int totalAmount;

    private int remainAmount;

    private LocalDateTime expiredAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public static PointEntity fromDomain(Point point) {
        return PointEntity.builder()
            .pointKey(point.getPointKey())
            .userKey(point.getUserKey())
            .status(point.getStatus())
            .rewardType(point.getRewardType())
            .totalAmount(point.getTotalAmount())
            .remainAmount(point.getRemainAmount())
            .expiredAt(point.getExpiredAt())
            .build();
    }

    public Point toDomain() {
        return Point.builder()
            .pointKey(this.pointKey)
            .userKey(this.userKey)
            .status(this.status)
            .rewardType(this.rewardType)
            .totalAmount(this.totalAmount)
            .remainAmount(this.remainAmount)
            .expiredAt(this.expiredAt)
            .createdAt(this.createdAt)
            .build();
    }
}
