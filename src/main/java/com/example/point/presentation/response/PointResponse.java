package com.example.point.presentation.response;

import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointRewardType;
import com.example.point.domain.model.PointStatus;
import java.time.LocalDateTime;
import lombok.Builder;

@Builder
public record PointResponse (

    Long pointKey,
    Long userKey,
    PointStatus status,
    PointRewardType rewardType,
    int totalAmount,
    int remainAmount,
    LocalDateTime expiredAt,
    LocalDateTime createdAt
) {
    public static PointResponse of(Point point) {
        return PointResponse.builder()
            .pointKey(point.getPointKey())
            .userKey(point.getUserKey())
            .status(point.getStatus())
            .rewardType(point.getRewardType())
            .totalAmount(point.getTotalAmount())
            .remainAmount(point.getRemainAmount())
            .expiredAt(point.getExpiredAt())
            .createdAt(point.getCreatedAt())
            .build();
    }
}
