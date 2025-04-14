package com.example.point.domain.model;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter @Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class PointHistory {

    private Long historyKey;

    private Point point;

    private PointHistoryActionType actionType;

    private int amount;

    private Long orderKey;

    private LocalDateTime createdAt;

    private Long relatedHistoryKey;

    public static PointHistory create(
        Point point, PointHistoryActionType actionType, int amount, Long orderKey, Long relatedHistoryKey) {

        return PointHistory.builder()
            .point(point)
            .actionType(actionType)
            .amount(amount)
            .orderKey(orderKey)
            .relatedHistoryKey(relatedHistoryKey)
            .build();
    }
}
