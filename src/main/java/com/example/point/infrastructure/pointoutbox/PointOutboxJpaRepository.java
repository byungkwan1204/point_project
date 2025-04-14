package com.example.point.infrastructure.pointoutbox;

import org.springframework.data.jpa.repository.JpaRepository;

public interface PointOutboxJpaRepository extends JpaRepository<PointOutboxEntity, Long> {}

