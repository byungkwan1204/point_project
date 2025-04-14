package com.example.point.infrastructure.pointoutbox;

import com.example.point.domain.model.PointOutbox;
import com.example.point.domain.model.PointOutboxStatus;
import com.example.point.domain.model.PointOutboxType;
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
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Table(name = "points_outbox")
@EntityListeners(AuditingEntityListener.class)
public class PointOutboxEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long outboxKey;

    @Enumerated(EnumType.STRING)
    private PointOutboxType type;

    @Enumerated(EnumType.STRING)
    private PointOutboxStatus status;

    private String payload;

    private LocalDateTime processedAt;

    @CreatedDate
    private LocalDateTime createdAt;

    public static PointOutboxEntity fromDomain(PointOutbox pointOutbox) {
        return PointOutboxEntity.builder()
            .outboxKey(pointOutbox.getOutboxKey())
            .type(pointOutbox.getType())
            .status(pointOutbox.getStatus())
            .payload(pointOutbox.getPayload())
            .processedAt(pointOutbox.getProcessedAt())
            .build();
    }

    public PointOutbox toDomain() {
        return PointOutbox.builder()
            .outboxKey(this.outboxKey)
            .type(this.type)
            .status(this.status)
            .payload(this.payload)
            .processedAt(this.processedAt)
            .createdAt(this.createdAt)
            .build();
    }
}
