package com.example.point.presentation.request;

import lombok.Builder;

@Builder
public record PointUseCancelRequest(

    Long userKey,
    Long orderKey
) {}
