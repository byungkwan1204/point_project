package com.example.point.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import com.example.point.domain.event.trigger.PointHistoryEvent;
import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointHistory;
import com.example.point.domain.model.PointHistoryActionType;
import com.example.point.domain.model.PointRewardType;
import com.example.point.domain.model.PointStatus;
import com.example.point.global.PointProperties;
import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.presentation.request.PointUseRequest;
import com.example.point.presentation.response.PointResponse;
import com.example.point.service.port.PointHistoryRepository;
import com.example.point.service.port.PointOutboxRepository;
import com.example.point.service.port.PointRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class PointServiceTest {

    @Mock
    PointRepository pointRepository;

    @Mock
    PointHistoryRepository pointHistoryRepository;

    @Mock
    PointOutboxRepository pointOutboxRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;

    @Mock
    ObjectMapper objectMapper;

    @Mock
    PointProperties pointProperties;

    @InjectMocks
    PointService pointService;

    @Captor
    ArgumentCaptor<PointHistoryEvent> captor;

    @DisplayName("포인트를 적립 할 수 있다.")
    @Test
    void canCreateByManual() throws Exception {

        // given
        LocalDateTime expiredAt = LocalDateTime.now().plusDays(30);

        Point newPoint =
            createPoint(1L, PointRewardType.MANUAL, 1000, expiredAt);

        PointCreateRequest request = new PointCreateRequest(1L, 1000, expiredAt, PointRewardType.MANUAL);

        given(pointProperties.getMaxFreeAmount()).willReturn(100000);
        given(pointRepository.save(any())).willReturn(newPoint);

        ReflectionTestUtils.setField(newPoint, "pointKey", 1L);

        // when
        PointResponse savePoint = pointService.savePoint(request);

        // then
        assertThat(savePoint.pointKey()).isEqualTo(1L);
        assertThat(savePoint.userKey()).isEqualTo(1L);
        assertThat(savePoint.rewardType()).isEqualTo(PointRewardType.MANUAL);
        assertThat(savePoint.status()).isEqualTo(PointStatus.ACTIVE);
        assertThat(savePoint.totalAmount()).isEqualTo(1000);
        assertThat(savePoint.expiredAt()).isEqualTo(expiredAt);

        verify(eventPublisher).publishEvent(captor.capture());
    }

    @DisplayName("적립된 포인트를 취소할 수 있다.")
    @Test
    void canCancel() throws Exception {

        // given
        Point point =
            createPoint(1L, PointRewardType.MANUAL, 1000, LocalDateTime.now().plusDays(30));

        PointHistory pointHistory = PointHistory.create(point, PointHistoryActionType.SAVE, point.getTotalAmount(), null, null);

        Point cPoint = Point.builder()
            .pointKey(point.getPointKey())
            .userKey(point.getUserKey())
            .status(PointStatus.CANCELED)
            .rewardType(point.getRewardType())
            .totalAmount(point.getTotalAmount())
            .remainAmount(0)
            .expiredAt(point.getExpiredAt())
            .build();

        given(pointHistoryRepository.findSavedByPointKey(anyLong())).willReturn(Optional.ofNullable(pointHistory));
        given(pointRepository.save(any())).willReturn(cPoint);

        // when
        PointResponse cancelPoint = pointService.saveCancelPoint(1L);

        // then
        assertThat(cancelPoint.status()).isEqualTo(PointStatus.CANCELED);
        assertThat(cancelPoint.remainAmount()).isZero();

        verify(eventPublisher).publishEvent(captor.capture());
    }

    @DisplayName("포인트를 사용할 수 있다.")
    @Test
    void canUsePoint()  throws Exception {

        // given
        Point point =
            createPoint(1L, PointRewardType.MANUAL, 1000, LocalDateTime.now().plusDays(30));

        List<Point> points = new ArrayList<>();
        points.add(point);

        PointUseRequest useRequest = new PointUseRequest(1L, 1234L, 600);

        given(pointRepository.findActivatePointsByUserKey(anyLong()))
            .willReturn(points);

        // when
        List<PointResponse> usePoints =  pointService.usePoint(useRequest);

        // then
        assertThat(usePoints).hasSize(1);
        assertThat(usePoints)
            .extracting(PointResponse::userKey).containsExactly(1L);
        assertThat(usePoints)
            .extracting(PointResponse::remainAmount).containsExactly(400);

        verify(eventPublisher).publishEvent(captor.capture());
    }

    @DisplayName("여러개의 포인트가 있을 경우 수기 지급 포인트를 우선으로 사용하고, 만료일이 짧은 순서로 사용된다.")
    @Test
    void canUseOfPriority() throws Exception {

        // given
        Point firstUsePoint =
            createPoint(1L, PointRewardType.MANUAL, 500, LocalDateTime.now().plusDays(5));
        Point secondUsePoint =
            createPoint(1L, PointRewardType.MANUAL, 500, LocalDateTime.now().plusDays(30));
        Point thirdUsePoint =
            createPoint(1L, PointRewardType.OTHER, 500, LocalDateTime.now().plusDays(3));
        Point fourthUsePoint =
            createPoint(1L, PointRewardType.OTHER, 500, LocalDateTime.now().plusDays(60));

        List<Point> points = new ArrayList<>();
        points.add(firstUsePoint);
        points.add(secondUsePoint);
        points.add(thirdUsePoint);
        points.add(fourthUsePoint);

        PointUseRequest useRequest = new PointUseRequest(1L, 1234L, 1300);

        given(pointRepository.findActivatePointsByUserKey(anyLong())).willReturn(points);

        // when
        List<PointResponse> usePoints = pointService.usePoint(useRequest);

        // then
        assertThat(usePoints).hasSize(3);
        assertThat(usePoints)
            .extracting(PointResponse::remainAmount).containsExactly(0,0,200);

        verify(eventPublisher, times(3)).publishEvent(captor.capture());
    }

    @DisplayName("사용한 포인트를 취소 시, 만료되지 않았다면 복원된다.")
    @Test
    void canUseCancel() throws Exception {

        // given
        Point usePoint =
            Point.create(new PointCreateRequest(1L, 1000, LocalDateTime.now().plusDays(30), PointRewardType.MANUAL), 100000);

        usePoint.use(500);

        PointHistory useHistory =
            PointHistory.create(usePoint, PointHistoryActionType.USE, 500, 1234L, null);

        List<PointHistory> pointHistories = new ArrayList<>();
        pointHistories.add(useHistory);

        PointUseCancelRequest useCancelRequest = new PointUseCancelRequest(1L, 1234L);

        given(pointHistoryRepository.findAllUsedByOrderKey(any())).willReturn(pointHistories);

        // when
        List<PointResponse> cancelPoints = pointService.useCancelPoint(useCancelRequest);

        // then
        assertThat(cancelPoints).hasSize(1);
        assertThat(cancelPoints)
            .extracting(PointResponse::remainAmount).containsExactly(1000);

        verify(eventPublisher).publishEvent(captor.capture());
    }

    @DisplayName("사용한 포인트를 취소 시, 만료된 포인트는 복원하지 않고 새로 적립 한다.")
    @Test
    void recreatePointWhenUseCancelExpiredPoint() throws Exception {

        // given
        Point expiredPoint = Point.builder()
            .pointKey(1L)
            .userKey(1L)
            .status(PointStatus.EXPIRED)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(0)
            .expiredAt(LocalDateTime.now().minusDays(1))
            .createdAt(LocalDateTime.now().minusDays(31))
            .build();

        Point activePoint = Point.builder()
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(1000)
            .expiredAt(LocalDateTime.now().plusDays(365))
            .createdAt(LocalDateTime.now())
            .build();

        PointHistory expiredPointHistory =
            PointHistory.create(expiredPoint, PointHistoryActionType.USE, 1000, 1234L, null);

        ReflectionTestUtils.setField(expiredPointHistory, "historyKey", 10L);

        List<PointHistory> pointHistories = new ArrayList<>();
        pointHistories.add(expiredPointHistory);

        given(pointProperties.getMaxFreeAmount()).willReturn(100000);
        given(pointHistoryRepository.findAllUsedByOrderKey(any())).willReturn(pointHistories);
        given(pointRepository.save(any())).willReturn(activePoint);

        ReflectionTestUtils.setField(activePoint, "pointKey", 2L);

        PointUseCancelRequest request = new PointUseCancelRequest(1L, 1234L);

        // when
        List<PointResponse> cancelPoints = pointService.useCancelPoint(request);

        // then
        assertThat(cancelPoints).hasSize(1);
        assertThat(cancelPoints).extracting(PointResponse::pointKey, PointResponse::remainAmount)
            .containsExactlyInAnyOrder(
                Tuple.tuple(2L, 1000));

        verify(eventPublisher, times(2)).publishEvent(captor.capture());
    }

    private Point createPoint(Long pointKey, PointRewardType rewardType, int amount, LocalDateTime expiredAt) {
        return Point.builder()
            .pointKey(pointKey)
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(rewardType)
            .totalAmount(amount)
            .remainAmount(amount)
            .expiredAt(expiredAt)
            .build();
    }
}