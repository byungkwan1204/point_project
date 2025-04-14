package com.example.point.presentation.request;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record PointUseCancelRequest(

    @NotNull(message = "사용자 KEY는 필수 입니다.")
    Long userKey,

    @NotNull(message = "주문 KEY는 필수 입니다.")
    Long orderKey
) {}
