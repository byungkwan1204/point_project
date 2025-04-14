package com.example.point.infrastructure.pointoutbox;

import com.example.point.domain.model.PointOutbox;
import com.example.point.service.port.PointOutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointOutboxRepositoryImpl implements PointOutboxRepository {

    private final PointOutboxJpaRepository jpaRepository;

    @Override
    public void save(PointOutbox pointOutbox) {
        jpaRepository.save(PointOutboxEntity.fromDomain(pointOutbox));
    }

    @Override
    public void deleteAll() {
        jpaRepository.deleteAll();
    }
}
