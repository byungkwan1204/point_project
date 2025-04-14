package com.example.point.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class PointOutbox {

    private Long outboxKey;

    private PointOutboxType type;

    private PointOutboxStatus status;

    private String payload;

    private LocalDateTime processedAt;

    private LocalDateTime createdAt;

    public static PointOutbox create(PointOutboxType type, String payload) {
        return PointOutbox.builder()
            .type(type)
            .status(PointOutboxStatus.PENDING)
            .payload(payload)
            .build();
    }
}
