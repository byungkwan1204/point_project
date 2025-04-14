package com.example.point.infrastructure.point;

import com.example.point.domain.model.Point;
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
    public Optional<Point> findByPointKey(Long pointKey) {
        return jpaRepository.findByPointKey(pointKey)
            .map(PointEntity::toDomain);
    }

    @Override
    public List<Point> findActivatePointsByUserKey(Long userKey) {
        return jpaRepository.findActivatePointsByUserKey(userKey).stream()
            .map(PointEntity::toDomain)
            .collect(Collectors.toList());
    }

    @Override
    public Point save(Point point) {

        PointEntity savedPoint = jpaRepository.save(PointEntity.fromDomain(point));

        return savedPoint.toDomain();
    }
}
