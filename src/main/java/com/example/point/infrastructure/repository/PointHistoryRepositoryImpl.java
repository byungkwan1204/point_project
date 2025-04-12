package com.example.point.infrastructure.repository;

import com.example.point.domain.model.PointHistory;
import com.example.point.infrastructure.entity.PointHistoryEntity;
import com.example.point.service.port.PointHistoryRepository;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointHistoryRepositoryImpl implements PointHistoryRepository {

    private final PointHistoryJpaRepository jpaRepository;

    @Override
    public List<PointHistory> findAllUsageByOrderKey(Long orderKey) {
        return jpaRepository.findAllUsageByOrderKey(orderKey).stream()
            .map(PointHistoryEntity::toDomain)
            .toList();
    }

    @Override
    public Optional<PointHistory> findSaveByPointKey(Long pointKey) {
        return jpaRepository.findSaveByPointKey(pointKey)
            .map(PointHistoryEntity::toDomain);
    }

    @Override
    public PointHistory save(PointHistory pointHistory) {

        PointHistoryEntity savedPointHistory =
            jpaRepository.save(PointHistoryEntity.fromDomain(pointHistory));

        return savedPointHistory.toDomain();
    }
}
