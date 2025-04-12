package com.example.point.presentation.request;

import lombok.Builder;

@Builder
public record PointUseRequest(

    Long userKey,
    Long orderKey,
    int amount

) {}
