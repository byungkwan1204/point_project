package com.example.point.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointOutboxType {

    POINT_SAVE        ("적립 이벤트"),
    POINT_SAVE_CANCEL ("적립 취소 이벤트"),
    POINT_USE         ("사용 이벤트"),
    POINT_USE_CANCEL  ("사용 취소 이벤트"),
    POINT_EXPIRED     ("만료 이벤트");

    private final String desc;
}
