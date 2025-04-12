package com.example.point.domain.event.listener;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.verify;

import com.example.point.domain.event.trigger.PointHistoryEvent;
import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointHistory;
import com.example.point.domain.model.PointHistoryActionType;
import com.example.point.domain.model.PointRewardType;
import com.example.point.domain.model.PointStatus;
import com.example.point.service.port.PointHistoryRepository;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PointHistoryListenerTest {

    @Mock
    PointHistoryRepository pointHistoryRepository;
    
    @InjectMocks
    PointHistoryListener pointHistoryListener;
    
    @DisplayName("SAVE 이벤트를 수신하면 적립 이력을 저장한다.")
    @Test
    void shouldSavePointHistory() {
        // given
        Point point = Point.builder()
            .pointKey(1L)
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(1000)
            .expiredDate(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build();

        PointHistoryEvent event = new PointHistoryEvent(point, 1000, PointHistoryActionType.SAVE, null, null);

        // when
        pointHistoryListener.listen(event);

        // then
        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(captor.capture());

        PointHistory saved = captor.getValue();
        assertThat(saved.getPoint()).isEqualTo(point);
        assertThat(saved.getActionType()).isEqualTo(PointHistoryActionType.SAVE);
        assertThat(saved.getAmount()).isEqualTo(1000);
    }

    @DisplayName("SAVE_CANCEL 이벤트를 수신하면 적립 취소 이력을 저장한다.")
    @Test
    void shouldSaveCancelPointHistory() {
        // given
        Point point = Point.builder()
            .pointKey(1L)
            .userKey(1L)
            .status(PointStatus.CANCELED)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(0)
            .expiredDate(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build();

        Long relatedHistoryKey = 1L;

        PointHistoryEvent event = new PointHistoryEvent(point, 1000, PointHistoryActionType.SAVE_CANCEL, relatedHistoryKey, null);

        // when
        pointHistoryListener.listen(event);

        // then
        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(captor.capture());

        PointHistory saved = captor.getValue();
        assertThat(saved.getActionType()).isEqualTo(PointHistoryActionType.SAVE_CANCEL);
        assertThat(saved.getAmount()).isEqualTo(1000);
        assertThat(saved.getPoint()).isEqualTo(point);
        assertThat(saved.getRelatedHistoryKey()).isEqualTo(relatedHistoryKey);
    }

    @DisplayName("USE 이벤트를 수신하면 사용 이력을 저장한다.")
    @Test
    void shouldUsePointHistory() {
        // given
        Point point = Point.builder()
            .pointKey(1L)
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(400)
            .expiredDate(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build();

        Long orderKey = 1234L;

        PointHistoryEvent event = new PointHistoryEvent(point, 600, PointHistoryActionType.USE, null, orderKey);

        // when
        pointHistoryListener.listen(event);

        // then
        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(captor.capture());

        PointHistory saved = captor.getValue();
        assertThat(saved.getActionType()).isEqualTo(PointHistoryActionType.USE);
        assertThat(saved.getAmount()).isEqualTo(600);
        assertThat(saved.getPoint()).isEqualTo(point);
        assertThat(saved.getOrderKey()).isEqualTo(orderKey);
    }

    @DisplayName("USE_CANCEL 이벤트를 수신하면 사용 취소 이력을 저장한다.")
    @Test
    void shouldUseCancelPointHistory() {
        // given
        Point point = Point.builder()
            .pointKey(1L)
            .userKey(1L)
            .status(PointStatus.ACTIVE)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(1000)
            .expiredDate(LocalDateTime.now().plusDays(30))
            .createdAt(LocalDateTime.now())
            .build();

        Long orderKey = 1234L;
        Long relatedHistoryKey = 1L;

        PointHistoryEvent event = new PointHistoryEvent(point, 600, PointHistoryActionType.USE_CANCEL, relatedHistoryKey, orderKey);

        // when
        pointHistoryListener.listen(event);

        // then
        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(captor.capture());

        PointHistory saved = captor.getValue();
        assertThat(saved.getActionType()).isEqualTo(PointHistoryActionType.USE_CANCEL);
        assertThat(saved.getAmount()).isEqualTo(600);
        assertThat(saved.getPoint()).isEqualTo(point);
        assertThat(saved.getOrderKey()).isEqualTo(orderKey);
        assertThat(saved.getRelatedHistoryKey()).isEqualTo(relatedHistoryKey);
    }

    @DisplayName("EXPIRED 이벤트를 수신하면 만료 이력을 저장한다.")
    @Test
    void shouldExpirePointHistory() {
        // given
        Point point = Point.builder()
            .pointKey(1L)
            .userKey(1L)
            .status(PointStatus.EXPIRED)
            .rewardType(PointRewardType.MANUAL)
            .totalAmount(1000)
            .remainAmount(200)
            .expiredDate(LocalDateTime.now().minusDays(1))
            .createdAt(LocalDateTime.now().minusDays(31))
            .build();

        PointHistoryEvent event = new PointHistoryEvent(point, 200, PointHistoryActionType.EXPIRED, null, null);

        // when
        pointHistoryListener.listen(event);

        // then
        ArgumentCaptor<PointHistory> captor = ArgumentCaptor.forClass(PointHistory.class);
        verify(pointHistoryRepository).save(captor.capture());

        PointHistory saved = captor.getValue();
        assertThat(saved.getActionType()).isEqualTo(PointHistoryActionType.EXPIRED);
        assertThat(saved.getAmount()).isEqualTo(200);
        assertThat(saved.getPoint()).isEqualTo(point);
    }
}