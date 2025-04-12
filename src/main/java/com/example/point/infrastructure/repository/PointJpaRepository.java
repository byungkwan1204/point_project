package com.example.point.infrastructure.repository;

import com.example.point.infrastructure.entity.PointEntity;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PointJpaRepository extends JpaRepository<PointEntity, Long> {

    List<PointEntity> findUsablePointsByUserKey(Long userKey);
}
