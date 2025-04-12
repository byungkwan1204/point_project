package com.example.point.service;

import com.example.point.domain.event.trigger.PointHistoryEvent;
import com.example.point.domain.model.PointHistory;
import com.example.point.domain.model.PointRewardType;
import com.example.point.presentation.request.PointCreateRequest;
import com.example.point.presentation.request.PointUseRequest;
import com.example.point.presentation.request.PointUseCancelRequest;
import com.example.point.domain.model.Point;
import com.example.point.service.port.PointHistoryRepository;
import com.example.point.service.port.PointRepository;
import com.example.point.service.usecase.PointUsecase;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PointService implements PointUsecase {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Point savePoint(PointCreateRequest pointCreateRequest) {

        Point newPoint = pointRepository.save(Point.create(pointCreateRequest));

        // 포인트 적립 이력 기록 이벤트 발행
        eventPublisher.publishEvent(PointHistoryEvent.save(newPoint, pointCreateRequest.amount()));

        return newPoint;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Point cancelSavePoint(Long pointKey) {

        PointHistory pointHistory = pointHistoryRepository.findSaveByPointKey(pointKey)
            .orElseThrow(() -> new IllegalArgumentException("포인트가 적립된 이력이 없습니다."));

        Point point = pointHistory.getPoint();

        // 포인트 적립 취소
        point.cancel();

        // DB 반영
        Point cancelPoint = pointRepository.save(point);

        // 포인트 적립 취소 이력 기록 이벤트 발행
        eventPublisher.publishEvent(PointHistoryEvent.saveCancel(cancelPoint, point.getTotalAmount(), pointHistory.getHistoryKey()));

        return cancelPoint;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Point> usePoint(PointUseRequest pointUseRequest) {

        int totalUsePoint = pointUseRequest.amount();

        List<Point> points = pointRepository.findUsablePointsByUserKey(pointUseRequest.userKey());

        // 현재 사용 가능한 포인트
        int totalAvailablePoint = points.stream()
            .mapToInt(Point::getRemainAmount)
            .sum();

        if (totalAvailablePoint < totalUsePoint) {
            throw new IllegalArgumentException("사용 가능한 포인트가 부족합니다.");
        }

        // 수기 적립 -> 만료일이 빠른 순 정렬
        points.sort(Comparator.comparing(this::getUsePriority)
                        .thenComparing(Point::getExpiredDate));

        List<Point> usedPoints = new ArrayList<>();

        for (Point point : points) {

            if (totalUsePoint == 0) {
                break;
            }

            // 총 사용하려는 포인트를 기준으로 1개의 적립된 포인트에서 사용가능한 포인트 계산
            int usePoint = Math.min(totalUsePoint, point.getRemainAmount());

            // 포인트 사용 및 포인트 잔여 포인트 차감
            point.use(usePoint);

            // DB 반영
            pointRepository.save(point);

            // 포인트 사용 이력 기록 이벤트 발행
            eventPublisher.publishEvent(PointHistoryEvent.use(point, usePoint, pointUseRequest.orderKey()));

            usedPoints.add(point);

            // 총 사용하려는 포인트에서 1개의 적립된 포인트에서 사용된 포인트 만큼 차감
            totalUsePoint -= usePoint;
        }

        return usedPoints;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public List<Point> cancelUsePoint(PointUseCancelRequest pointUseCancelRequest) {

        List<PointHistory> usedHistories = pointHistoryRepository.findAllUsageByOrderKey(pointUseCancelRequest.orderKey());

        if (usedHistories.isEmpty()) {
            throw new IllegalArgumentException("포인트를 사용한 이력이 존재하지 않습니다.");
        }

        List<Point> useCanceledPoints = new ArrayList<>();

        for (PointHistory usedHistory : usedHistories) {

            Point point = usedHistory.getPoint();

            // 만료 -> 새 포인트 적립
            if (point.isExpired()) {

                Point recreatePoint =
                    Point.create(new PointCreateRequest(point.getUserKey(), usedHistory.getAmount(), null, PointRewardType.OTHER));

                Point savedPoint = pointRepository.save(recreatePoint);

                // 새 포인트 적립 이력 기록 이벤트 발행
                eventPublisher.publishEvent(PointHistoryEvent.reSave(savedPoint, usedHistory.getAmount(), usedHistory.getHistoryKey()));

                useCanceledPoints.add(savedPoint);

            // 만료가 아닌 경우 -> 포인트 복구 처리
            } else {

                point.useCancel(usedHistory.getAmount());
                // DB 반영
                pointRepository.save(point);
                useCanceledPoints.add(point);
            }

            // 복원 이력 기록 이벤트 발행
            eventPublisher.publishEvent(
                PointHistoryEvent.useCancel(point, usedHistory.getAmount(), usedHistory.getHistoryKey(), pointUseCancelRequest.orderKey()));
        }

        return useCanceledPoints;
    }

    private int getUsePriority(Point point) {
        return switch(point.getRewardType()) {
            case MANUAL -> 0;
            case OTHER -> 1;
        };
    }
}
