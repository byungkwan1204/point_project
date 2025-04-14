package com.example.point.infrastructure.point;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<PointEntity> findByPointKey(Long pointKey);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from PointEntity p " +
           "where p.status = 'ACTIVE' " +
           "and p.expiredAt > now() " +
           "and p.userKey = :userKey")
    List<PointEntity> findActivatePointsByUserKey(Long userKey);
}
