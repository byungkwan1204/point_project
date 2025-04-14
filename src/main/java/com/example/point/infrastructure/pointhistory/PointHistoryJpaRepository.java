package com.example.point.infrastructure.pointhistory;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointHistoryJpaRepository extends JpaRepository<PointHistoryEntity, Long> {

    @Query("select ph from PointHistoryEntity ph join fetch ph.point p " +
           "where ph.actionType = 'USE' " +
           "and ph.orderKey = :orderKey")
    List<PointHistoryEntity> findAllUsageByOrderKey(Long orderKey);

    @Query("select ph from PointHistoryEntity ph join fetch ph.point p " +
           "where p.status = 'ACTIVE' " +
           "and ph.actionType = 'SAVE' " +
           "and p.pointKey = :pointKey")
    Optional<PointHistoryEntity> findSavedByPointKey(Long pointKey);

    @Query("select ph from PointHistoryEntity ph join fetch ph.point p " +
           "where p.status = 'CANCELED' " +
           "and ph.actionType = 'SAVE_CANCEL' " +
           "and p.pointKey = :pointKey")
    Optional<PointHistoryEntity> findSaveCanceledByPointKey(Long pointKey);
}
