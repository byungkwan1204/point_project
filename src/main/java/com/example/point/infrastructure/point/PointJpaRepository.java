package com.example.point.infrastructure.point;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {

    @Query("select p from PointEntity p " +
           "where p.status = 'ACTIVE' " +
           "and p.expiredAt > now() " +
           "and p.userKey = :userKey")
    List<PointEntity> findUsablePointsByUserKey(Long userKey);
}
