package com.example.point.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointStatus {

    ACTIVE   ("활성"),
    CANCELED ("적립 취소"),
    EXPIRED  ("만료");

    private final String desc;
}
