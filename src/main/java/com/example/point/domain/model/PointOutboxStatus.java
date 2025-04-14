package com.example.point.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointOutboxStatus {

    PENDING   ("대기"),
    COMPLETED ("완료");

    private final String desc;
}
