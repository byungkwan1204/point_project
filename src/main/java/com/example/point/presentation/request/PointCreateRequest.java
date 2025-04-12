package com.example.point.presentation.request;

import com.example.point.domain.model.PointRewardType;
import java.time.LocalDateTime;
import lombok.Builder;

public record PointCreateRequest(

    Long userKey,
    int amount,
    LocalDateTime expiredDate,
    PointRewardType rewardType

) {}
