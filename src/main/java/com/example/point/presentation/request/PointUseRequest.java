package com.example.point.presentation.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PointUseRequest(

    @NotNull(message = "사용자 KEY는 필수 입니다.")
    Long userKey,

    @NotNull(message = "주문 KEY는 필수 입니다.")
    Long orderKey,

    @Min(value = 0, message = "사용할 포인트는 0 이상 이어야 합니다.")
    int amount

) {}
