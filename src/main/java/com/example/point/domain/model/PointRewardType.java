package com.example.point.domain.model;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum PointRewardType {

    OTHER ("그외 적립"),
    MANUAL ("수기 적립");

    private final String desc;
}
