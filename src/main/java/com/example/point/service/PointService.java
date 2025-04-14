package com.example.point.service;

import com.example.point.domain.event.trigger.PointHistoryEvent;
import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointHistory;
import com.example.point.domain.model.PointOutbox;
import com.example.point.domain.model.PointOutboxType;
import com.example.point.domain.model.PointRewardType;
import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.presentation.request.PointUseRequest;
import com.example.point.presentation.response.PointResponse;
import com.example.point.service.port.PointHistoryRepository;
import com.example.point.service.port.PointOutboxRepository;
import com.example.point.service.port.PointRepository;
import com.example.point.service.usecase.PointUsecase;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService implements PointUsecase {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final PointOutboxRepository pointOutboxRepository;
    private final ApplicationEventPublisher eventPublisher;

    private final ObjectMapper objectMapper;

    @Transactional
    @Override
    public PointResponse savePoint(PointCreateRequest pointCreateRequest) throws Exception {

        Point newPoint = pointRepository.save(Point.create(pointCreateRequest));

        // 포인트 적립 이벤트 발행 내용 DB 저장
        pointOutboxRepository.save(PointOutbox.create(PointOutboxType.POINT_SAVE, objectMapper.writeValueAsString(newPoint)));

        // 포인트 적립 이력 기록 이벤트 발행
        eventPublisher.publishEvent(PointHistoryEvent.save(newPoint.getPointKey(), pointCreateRequest.amount()));

        return PointResponse.of(newPoint);
    }

    @Transactional
    @Override
    public PointResponse saveCancelPoint(Long pointKey) throws Exception {

        PointHistory pointHistory = pointHistoryRepository.findSavedByPointKey(pointKey)
            .orElseThrow(() -> new IllegalArgumentException("포인트가 적립된 이력이 없습니다."));

        Point point = pointHistory.getPoint();

        // 포인트 적립 취소
        point.cancel();

        // DB 반영
        Point cancelPoint = pointRepository.save(point);

        // 포인트 적립 취소 이벤트 발행 내용 DB 저장
        pointOutboxRepository.save(PointOutbox.create(PointOutboxType.POINT_SAVE_CANCEL, objectMapper.writeValueAsString(cancelPoint)));

        // 포인트 적립 취소 이력 기록 이벤트 발행
        eventPublisher.publishEvent(PointHistoryEvent.saveCancel(cancelPoint.getPointKey(), point.getTotalAmount(), pointHistory.getHistoryKey()));

        return PointResponse.of(cancelPoint);
    }

    @Transactional
    @Override
    public List<PointResponse> usePoint(PointUseRequest pointUseRequest) throws Exception {

        int totalUsePoint = pointUseRequest.amount();

        List<Point> usablePoints = pointRepository.findUsablePointsByUserKey(pointUseRequest.userKey());

        // 현재 사용 가능한 포인트
        int totalAvailablePoint = usablePoints.stream()
            .mapToInt(Point::getRemainAmount)
            .sum();

        if (totalAvailablePoint < totalUsePoint) {
            throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
        }

        // 수기 적립 -> 만료일이 빠른 순 정렬
        usablePoints.sort(Comparator.comparing(this::getUsePriority).thenComparing(Point::getExpiredAt));

        List<Point> usedPoints = new ArrayList<>();

        for (Point usablePoint : usablePoints) {

            if (totalUsePoint == 0) {
                break;
            }

            // 총 사용하려는 포인트를 기준으로 1개의 적립된 포인트에서 사용가능한 포인트 계산
            int usePoint = Math.min(totalUsePoint, usablePoint.getRemainAmount());

            // 포인트 사용 및 포인트 잔여 포인트 차감
            usablePoint.use(usePoint);

            // DB 반영
            pointRepository.save(usablePoint);

            // 포인트 사용 이벤트 발행 내용 DB 저장
            pointOutboxRepository.save(PointOutbox.create(PointOutboxType.POINT_USE, objectMapper.writeValueAsString(usablePoint)));

            // 포인트 사용 이력 기록 이벤트 발행
            eventPublisher.publishEvent(PointHistoryEvent.use(usablePoint.getPointKey(), usePoint, pointUseRequest.orderKey()));

            usedPoints.add(usablePoint);

            // 총 사용하려는 포인트에서 1개의 적립된 포인트에서 사용된 포인트 만큼 차감
            totalUsePoint -= usePoint;
        }

        return usedPoints.stream()
            .map(PointResponse::of)
            .toList();
    }

    @Transactional
    @Override
    public List<PointResponse> useCancelPoint(PointUseCancelRequest pointUseCancelRequest) throws Exception {

        List<PointHistory> usedHistories = pointHistoryRepository.findAllUsedByOrderKey(pointUseCancelRequest.orderKey());

        if (usedHistories.isEmpty()) {
            throw new IllegalArgumentException("포인트를 사용한 이력이 존재하지 않습니다.");
        }

        List<Point> useCanceledPoints = new ArrayList<>();

        for (PointHistory usedHistory : usedHistories) {

            Point usedPoint = usedHistory.getPoint();

            // 만료 -> 새 포인트 적립
            if (usedPoint.isExpired()) {

                Point recreatePoint =
                    Point.create(new PointCreateRequest(usedPoint.getUserKey(), usedHistory.getAmount(), null, PointRewardType.OTHER));

                Point savedPoint = pointRepository.save(recreatePoint);

                // 포인트 적립 이벤트 발행 내용 DB 저장
                pointOutboxRepository.save(PointOutbox.create(PointOutboxType.POINT_SAVE, objectMapper.writeValueAsString(savedPoint)));

                // 새 포인트 적립 이력 기록 이벤트 발행
                eventPublisher.publishEvent(PointHistoryEvent.reSave(savedPoint.getPointKey(), usedHistory.getAmount(), usedHistory.getHistoryKey()));

                useCanceledPoints.add(savedPoint);

            // 만료가 아닌 경우 -> 포인트 복구 처리
            } else {

                // 포인트 사용 취소 처리
                usedPoint.useCancel(usedHistory.getAmount());

                // DB 반영
                pointRepository.save(usedPoint);

                useCanceledPoints.add(usedPoint);
            }

            // 포인트 사용 취소 이벤트 발행 내용 DB 저장
            pointOutboxRepository.save(PointOutbox.create(PointOutboxType.POINT_USE_CANCEL, objectMapper.writeValueAsString(usedPoint)));

            // 복원 이력 기록 이벤트 발행
            eventPublisher.publishEvent(
                PointHistoryEvent.useCancel(usedPoint.getPointKey(), usedHistory.getAmount(), usedHistory.getHistoryKey(), pointUseCancelRequest.orderKey()));
        }

        return useCanceledPoints.stream()
            .map(PointResponse::of)
            .toList();
    }

    private int getUsePriority(Point point) {
        return switch(point.getRewardType()) {
            case MANUAL -> 0;
            case OTHER -> 1;
        };
    }
}