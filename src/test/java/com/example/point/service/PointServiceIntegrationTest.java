package com.example.point.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.point.IntegrationTestSupport;
import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointHistory;
import com.example.point.domain.model.PointHistoryActionType;
import com.example.point.domain.model.PointRewardType;
import com.example.point.domain.model.PointStatus;
import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.presentation.request.PointUseRequest;
import com.example.point.presentation.response.PointResponse;
import com.example.point.service.port.PointHistoryRepository;
import com.example.point.service.port.PointRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.assertj.core.groups.Tuple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class PointServiceIntegrationTest extends IntegrationTestSupport {

    @Autowired
    PointService pointService;

    @Autowired
    PointRepository pointRepository;

    @Autowired
    PointHistoryRepository pointHistoryRepository;

    @BeforeEach
    void resetAutoIncrement() {
        entityManager.createNativeQuery("ALTER TABLE points ALTER COLUMN point_key RESTART WITH 1").executeUpdate();
        entityManager.createNativeQuery("ALTER TABLE points_history ALTER COLUMN history_key RESTART WITH 1").executeUpdate();
    }

    @DisplayName("포인트 적립 - 정상 적립되고 이력이 남는다.")
    @Test
    void savePointSuccess() throws Exception {

        // given
        PointCreateRequest request =
            new PointCreateRequest(1L, 1000, null, PointRewardType.MANUAL);

        // when
        PointResponse savedPoint = pointService.savePoint(request);

        // then
        assertThat(savedPoint).isNotNull();
        assertThat(savedPoint.userKey()).isEqualTo(1L);
        assertThat(savedPoint.status()).isEqualTo(PointStatus.ACTIVE);
        assertThat(savedPoint.rewardType()).isEqualTo(PointRewardType.MANUAL);
        assertThat(savedPoint.totalAmount()).isEqualTo(1000);
        assertThat(savedPoint.remainAmount()).isEqualTo(1000);
        assertThat(savedPoint.expiredAt().toLocalDate()).isEqualTo(LocalDateTime.now().plusDays(365).toLocalDate());
    }

    @DisplayName("포인트 적립 취소 - 적립된 포인트가 취소 처리되고 이력이 남는다")
    @Test
    void cancelPointSuccess() throws Exception {

        // given
        Point point = Point.builder()
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(1000)
            .expiredAt(LocalDateTime.now().plusDays(365))
            .createdAt(LocalDateTime.now())
            .build();

        point = pointRepository.save(point);

        PointHistory pointHistory = PointHistory.builder()
            .point(point)
            .actionType(PointHistoryActionType.SAVE)
            .amount(1000)
            .build();

        pointHistoryRepository.save(pointHistory);

        // when
        PointResponse cancelPoint = pointService.saveCancelPoint(1L);

        // then
        assertThat(cancelPoint).isNotNull();
        assertThat(cancelPoint.status()).isEqualTo(PointStatus.CANCELED);
        assertThat(cancelPoint.remainAmount()).isZero();
    }

    @DisplayName("포인트 사용 - 적립 포인트를 순서대로 사용하며 이력이 남는다")
    @Test
    void usePointSuccess() throws Exception {

        // given
        Point point1 = Point.builder()
            .userKey(150L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(500)
            .remainAmount(500)
            .expiredAt(LocalDateTime.now().plusDays(1))
            .createdAt(LocalDateTime.now())
            .build();

        Point point2 = Point.builder()
            .userKey(150L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.OTHER)
            .totalAmount(800)
            .remainAmount(800)
            .expiredAt(LocalDateTime.now().plusDays(2))
            .createdAt(LocalDateTime.now())
            .build();

        point1 = pointRepository.save(point1);
        point2 = pointRepository.save(point2);

        PointHistory pointHistory1 = PointHistory.builder()
            .point(point1)
            .actionType(PointHistoryActionType.SAVE)
            .amount(500)
            .build();

        PointHistory pointHistory2 = PointHistory.builder()
            .point(point2)
            .actionType(PointHistoryActionType.SAVE)
            .amount(800)
            .build();

        pointHistoryRepository.save(pointHistory1);
        pointHistoryRepository.save(pointHistory2);

        PointUseRequest request = new PointUseRequest(150L, 1234L, 1000);

        // when
        List<PointResponse> usePoints = pointService.usePoint(request);

        // then
        assertThat(usePoints).hasSize(2);
        assertThat(usePoints).extracting(PointResponse::pointKey, PointResponse::remainAmount)
            .containsExactlyInAnyOrder(Tuple.tuple(1L, 0),
                                       Tuple.tuple(2L, 300));
    }

    @DisplayName("포인트 사용 취소 - 만료 되기 전에는 복구된다.")
    @Test
    void useCancelPointRestoreIfNotExpired() throws Exception {

        // given
        Point point = Point.builder()
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(500)
            .remainAmount(200)
            .expiredAt(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build();

        point = pointRepository.save(point);

        PointHistory usedHistory = PointHistory.builder()
            .point(point)
            .actionType(PointHistoryActionType.USE)
            .amount(300)
            .orderKey(1234L)
            .build();

        pointHistoryRepository.save(usedHistory);

        PointUseCancelRequest request = new PointUseCancelRequest(1L, 1234L);

        // when
        List<PointResponse> useCanceledPoints = pointService.useCancelPoint(request);

        // then
        assertThat(useCanceledPoints).hasSize(1);
        assertThat(useCanceledPoints)
            .extracting(PointResponse::pointKey, PointResponse::remainAmount)
            .containsExactlyInAnyOrder(Tuple.tuple(1L, 500));
    }

    @DisplayName("포인트 사용 취소 - 만료된 경우 새 포인트 생성된다.")
    @Test
    void useCancelPointCreateNewIfExpired() throws Exception {

        // given
        Point expiredPoint = Point.builder()
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(500)
            .remainAmount(200)
            .expiredAt(LocalDateTime.now().minusDays(1))
            .createdAt(LocalDateTime.now().minusDays(5))
            .build();

        expiredPoint = pointRepository.save(expiredPoint);

        PointHistory usedHistory = PointHistory.builder()
            .point(expiredPoint)
            .actionType(PointHistoryActionType.USE)
            .amount(300)
            .orderKey(1234L)
            .build();

        pointHistoryRepository.save(usedHistory);

        PointUseCancelRequest request = new PointUseCancelRequest(1L, 1234L);

        // when
        List<PointResponse> useCanceledPoints = pointService.useCancelPoint(request);

        // then
        assertThat(useCanceledPoints).hasSize(1);
        assertThat(useCanceledPoints.get(0).pointKey()).isNotEqualTo(expiredPoint.getPointKey());
        assertThat(useCanceledPoints)
            .extracting(PointResponse::pointKey, PointResponse::remainAmount, PointResponse::status)
            .containsExactlyInAnyOrder(Tuple.tuple(2L, 300, PointStatus.ACTIVE));
    }
}
