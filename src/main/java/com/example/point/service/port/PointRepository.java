package com.example.point.service.port;

import com.example.point.domain.model.Point;
import java.util.List;
import java.util.Optional;

public interface PointRepository {

    Optional<Point> findById(Long key);

    List<Point> findUsablePointsByUserKey(Long userKey);

    Point save(Point point);

}
