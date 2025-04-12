package com.example.point.domain.event.listener;

import com.example.point.domain.event.trigger.PointHistoryEvent;
import com.example.point.domain.model.PointHistory;
import com.example.point.domain.model.PointHistoryActionType;
import com.example.point.service.port.PointHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PointHistoryListener {

    private final PointHistoryRepository pointHistoryRepository;

    @TransactionalEventListener(phase = TransactionPhase.BEFORE_COMMIT)
    public void listen(PointHistoryEvent event) {
        pointHistoryRepository.save(createHistory(event));
    }

    private PointHistory createHistory(PointHistoryEvent event) {
        return PointHistory.builder()
            .point(event.point())
            .actionType(event.actionType())
            .amount(event.amount())
            .relatedHistoryKey(event.relatedHistoryKey())
            .orderKey(event.orderKey())
            .build();
    }
}
