package com.example.point.infrastructure.entity;

import com.example.point.domain.model.PointHistory;
import com.example.point.domain.model.PointHistoryActionType;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
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
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "points_history")
@EntityListeners(AuditingEntityListener.class)
public class PointHistoryEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "point_key")
    private PointEntity point;

    private Long relatedHistoryKey;

    private PointHistoryActionType actionType;

    private int amount;

    private Long orderKey;

    @CreatedDate
    private LocalDateTime createdAt;

    public static PointHistoryEntity fromDomain(PointHistory pointHistory) {
        return PointHistoryEntity.builder()
            .point(PointEntity.fromDomain(pointHistory.getPoint()))
            .relatedHistoryKey(pointHistory.getRelatedHistoryKey())
            .actionType(pointHistory.getActionType())
            .amount(pointHistory.getAmount())
            .orderKey(pointHistory.getOrderKey())
            .build();
    }

    public PointHistory toDomain() {
        return PointHistory.builder()
            .historyKey(this.historyKey)
            .point(this.point.toDomain())
            .relatedHistoryKey(this.relatedHistoryKey)
            .actionType(this.actionType)
            .amount(this.amount)
            .orderKey(this.orderKey)
            .build();
    }
}
