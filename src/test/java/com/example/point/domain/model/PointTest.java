package com.example.point.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.point.presentation.request.PointCreateRequest;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
class PointTest {

    @DisplayName("포인트는 1포인트 이상, 10만 포인트 이하로만 적립 가능하다.")
    @Test
    void canCreatePointWithinLimit() {

        // given
        PointCreateRequest pointCreateRequest1 = createPointCommand(0, LocalDateTime.now().plusDays(30));
        PointCreateRequest pointCreateRequest2 = createPointCommand(100001, LocalDateTime.now().plusDays(30));

        // when && then
        assertThatThrownBy(() -> Point.create(pointCreateRequest1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("적립 포인트는 1 이상만 가능합니다.");

        assertThatThrownBy(() -> Point.create(pointCreateRequest2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("적립 포인트는 100,000 이하만 가능합니다.");
    }

    @DisplayName("포인트 만료일은 최소 1일 이상, 최대 5년 미만이어야 한다.")
    @Test
    void expireDateAtLeastOneDayNotMoreThanFiveYears() {

        // given
        PointCreateRequest pointCreateRequest1 = createPointCommand(1000, LocalDateTime.now());
        PointCreateRequest pointCreateRequest2 = createPointCommand(1000, LocalDateTime.now().plusYears(5).plusDays(1));

        // when && then
        assertThatThrownBy(() -> Point.create(pointCreateRequest1))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("만료일은 최소 1일 이상만 가능합니다.");

        assertThatThrownBy(() -> Point.create(pointCreateRequest2))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("만료일은 5년 이내만 가능합니다.");
    }

    @DisplayName("포인트를 만료 상태로 변경할 수 있다.")
    @Test
    void canChangeExpire() {

        // given
        PointCreateRequest pointCreateRequest = createPointCommand(1000, LocalDateTime.now().plusDays(30));
        Point point = Point.create(pointCreateRequest);

        // when
        point.expire();

        // then
        assertThat(point.getStatus()).isEqualTo(PointStatus.EXPIRED);
        assertThat(point.getRemainAmount()).isZero();
    }

    @DisplayName("포인트가 만료되었는지 확인할 수 있다.")
    @Test
    void checkIfPointIsExpired() {

        // given
        PointCreateRequest pointCreateRequest = createPointCommand(1000, LocalDateTime.now().plusDays(30));
        Point point = Point.create(pointCreateRequest);
        point.expire();

        // when
        boolean isExpired = point.isExpired();

        // then
        assertThat(isExpired).isTrue();
    }

    @DisplayName("포인트를 정상적으로 사용하면 사용가능 금액이 차감된다.")
    @Test
    void canUsePoint() {

        // given
        PointCreateRequest pointCreateRequest = createPointCommand(1000, LocalDateTime.now().plusDays(30));
        Point point = Point.create(pointCreateRequest);

        // when
        point.use(400);

        // then
        assertThat(point.getRemainAmount()).isEqualTo(600);
    }



    @DisplayName("사용 가능한 포인트보다 더 많이 사용하려고 하면 예외가 발생한다.")
    @Test
    void canNotUseMoreThanRemainAmount() {

        // given
        PointCreateRequest pointCreateRequest = createPointCommand(1000, LocalDateTime.now().plusDays(30));
        Point point = Point.create(pointCreateRequest);

        // when && then
        assertThatThrownBy(() -> point.use(1100))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("사용하려는 포인트가 잔여 포인트보다 많습니다.");
    }

    @DisplayName("사용 취소 시 만료되지 않았다면 사용가능 금액이 복원된다.")
    @Test
    void restoreOnUseCancelIfNotExpired() {

        // given
        PointCreateRequest pointCreateRequest = createPointCommand(1000, LocalDateTime.now().plusDays(30));
        Point point = Point.create(pointCreateRequest);
        point.use(600);

        // when
        point.useCancel(600);

        // then
        assertThat(point.getRemainAmount()).isEqualTo(1000);
    }

    @DisplayName("만료된 포인트는 복원되지 않는다.")
    @Test
    void canNotRestoreIfExpired() {

        // given
        PointCreateRequest pointCreateRequest = createPointCommand(1000, LocalDateTime.now().plusDays(30));
        Point point = Point.create(pointCreateRequest);
        point.use(1000);
        point.expire();

        // when
        point.useCancel(1000);

        // then
        assertThat(point.getRemainAmount()).isZero();
    }

    @DisplayName("비활성 포인트를 사용하려고 하면 예외가 발생한다.")
    @Test
    void canNotUseDeActivePoint() {

        // given
        PointCreateRequest pointCreateRequest = createPointCommand(1000, LocalDateTime.now().plusDays(30));
        Point point1 = Point.create(pointCreateRequest);
        Point point2 = Point.create(pointCreateRequest);

        point1.cancel();
        point2.expire();

        // when && then
        assertThatThrownBy(() -> point1.use(1000))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("비활성 포인트는 사용할 수 없습니다.");

        assertThatThrownBy(() -> point2.use(1000))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessage("비활성 포인트는 사용할 수 없습니다.");

    }

    private PointCreateRequest createPointCommand(int totalAmount, LocalDateTime expireDate) {
        return new PointCreateRequest(1L, totalAmount, expireDate, PointRewardType.MANUAL);
    }

}