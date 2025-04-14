package com.example.point.presentation.request;

import com.example.point.domain.model.PointRewardType;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

public record PointCreateRequest(

    @NotNull(message = "사용자 KEY는 필수 입니다.")
    Long userKey,

    @Min(value = 1, message = "포인트는 1 이상 이어야 합니다.")
    int amount,

    LocalDateTime expiredAt,

    @NotNull(message = "적립 유형은 필수 입니다. (수기 적립/그외 적립)")
    PointRewardType rewardType

) {}
