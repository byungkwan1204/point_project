package com.example.point.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointHistoryActionType {

    SAVE        ("적립"),
    SAVE_CANCEL ("적립 취소"),
    USE         ("사용"),
    USE_CANCEL  ("사용 취소"),
    EXPIRED     ("만료");

    private final String desc;
}
