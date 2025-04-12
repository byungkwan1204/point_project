package com.example.point.domain.model;

import com.example.point.presentation.request.PointCreateRequest;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.AbstractAggregateRoot;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Point {

    private Long pointKey;

    private Long userKey;

    private PointStatus status;

    private PointRewardType rewardType;

    private int totalAmount;

    private int remainAmount;

    private LocalDateTime expiredDate;

    private LocalDateTime createdAt;

    /**
     * <b> </b>
     */
    public static Point create(PointCreateRequest pointCreateRequest) {

        // 금액 유효성 검증 (1 <= 0 <= 100000)
        validDateTotalAmount(pointCreateRequest.amount());

        // 만료일 유효성 검증 (1일 이상, 5년 미만)
        if (pointCreateRequest.expiredDate() != null) {
            validateExpiredDate(pointCreateRequest.expiredDate());
        }

        return Point.builder()
            .userKey(pointCreateRequest.userKey())
            .status(PointStatus.ACTIVE)
            .rewardType(pointCreateRequest.rewardType())
            .totalAmount(pointCreateRequest.amount())
            .remainAmount(pointCreateRequest.amount())
            .expiredDate(
                pointCreateRequest.expiredDate() == null ?
                    LocalDateTime.now().plusDays(365) : pointCreateRequest.expiredDate())
            .build();
    }

    public void cancel() {
        if (this.remainAmount != this.totalAmount) {
            throw new IllegalArgumentException("이미 사용된 포인트는 취소할 수 없습니다.");
        }

        this.status = PointStatus.CANCELED;
        this.remainAmount = 0;
    }

    public void use(int useAmount) {
        if (this.status != PointStatus.ACTIVE) {
            throw new IllegalArgumentException("비활성 포인트는 사용할 수 없습니다.");
        }

        if (useAmount > this.remainAmount) {
            throw new IllegalArgumentException("사용하려는 포인트가 잔여 포인트보다 많습니다.");
        }

        this.remainAmount -= useAmount;
    }

    public void useCancel(int useAmount) {
        if (isExpired()) {
            return;
        }

        this.remainAmount += useAmount;
    }

    public void expire() {
        this.status = PointStatus.EXPIRED;
        this.remainAmount = 0;
    }

    public boolean isExpired() {
        return this.status == PointStatus.EXPIRED || LocalDateTime.now().isAfter(this.expiredDate);
    }
    public boolean isCanceled() {
        return this.status == PointStatus.CANCELED;
    }

    // 금액 유효성 검증 (1 <= 0 <= 100000)
    private static void validDateTotalAmount(int totalAmount) {
        if (totalAmount < 1) {
            throw new IllegalArgumentException("적립 포인트는 1 이상만 가능합니다.");
        }

        if (totalAmount > 100000) {
            throw new IllegalArgumentException("적립 포인트는 100,000 이하만 가능합니다.");
        }
    }

    // 만료일 유효성 검증 (1일 이상, 5년 미만)
    private static void validateExpiredDate(LocalDateTime expiredDate) {

        LocalDateTime now = LocalDateTime.now();

        if (ChronoUnit.DAYS.between(now, expiredDate) < 1) {
            throw new IllegalArgumentException("만료일은 최소 1일 이상만 가능합니다.");
        }

        if (expiredDate.isAfter(now.plusYears(5))) {
            throw new IllegalArgumentException("만료일은 5년 이내만 가능합니다.");
        }
    }
}
