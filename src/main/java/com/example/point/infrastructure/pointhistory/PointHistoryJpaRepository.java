package com.example.point.infrastructure.pointhistory;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ph from PointHistoryEntity ph " +
           "where ph.actionType = 'USE' " +
           "and ph.orderKey = :orderKey")
    List<PointHistoryEntity> findAllUsageByOrderKey(Long orderKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select ph from PointHistoryEntity ph " +
           "where ph.point.pointKey = :pointKey " +
           "and ph.point.status = 'ACTIVE' " +
           "and ph.actionType = 'SAVE'")
    Optional<PointHistoryEntity> findSavedByPointKey(Long pointKey);
}
