package com.example.point.domain.event.listener;

import com.example.point.domain.event.trigger.PointHistoryEvent;
import com.example.point.domain.model.Point;
import com.example.point.domain.model.PointHistory;
import com.example.point.service.port.PointHistoryRepository;
import com.example.point.service.port.PointRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointHistoryListener {

    private final PointRepository pointRepository;
    private final PointHistoryRepository pointHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void listen(PointHistoryEvent event) {

        Point point = pointRepository.findByPointKey(event.pointKey())
            .orElseThrow(() -> new IllegalArgumentException("포인트가 존재하지 않습니다."));

        pointHistoryRepository.save(createHistory(point, event));
    }

    private PointHistory createHistory(Point point, PointHistoryEvent event) {
        return PointHistory.builder()
            .point(point)
            .actionType(event.actionType())
            .amount(event.amount())
            .relatedHistoryKey(event.relatedHistoryKey())
            .orderKey(event.orderKey())
            .build();
    }
}
