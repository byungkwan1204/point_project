package com.example.point.infrastructure.repository;

import com.example.point.infrastructure.entity.PointHistoryEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryEntity, Long> {

    List<PointHistoryEntity> findAllUsageByOrderKey(Long orderKey);

    Optional<PointHistoryEntity> findSaveByPointKey(Long pointKey);
}
