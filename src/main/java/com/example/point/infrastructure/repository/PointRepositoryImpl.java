package com.example.point.infrastructure.repository;

import static java.util.stream.Collectors.toList;

import com.example.point.domain.model.Point;
import com.example.point.infrastructure.entity.PointEntity;
import com.example.point.service.port.PointRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PointRepositoryImpl implements PointRepository {

    private final PointJpaRepository jpaRepository;

    @Override
    public Optional<Point> findById(Long key) {
        return jpaRepository.findById(key)
            .map(PointEntity::toDomain);
    }

    @Override
    public List<Point> findUsablePointsByUserKey(Long userKey) {
        return jpaRepository.findUsablePointsByUserKey(userKey).stream()
            .map(PointEntity::toDomain)
            .toList();
    }

    @Override
    public Point save(Point point) {

        PointEntity savedPoint = jpaRepository.save(PointEntity.fromDomain(point));

        return savedPoint.toDomain();
    }
}
