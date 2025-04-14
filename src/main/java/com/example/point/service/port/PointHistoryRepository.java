package com.example.point.service.port;

import com.example.point.domain.model.PointHistory;
import java.util.List;
import java.util.Optional;

public interface PointHistoryRepository {

    List<PointHistory> findAllUsedByOrderKey(Long orderKey);

    Optional<PointHistory> findSavedByPointKey(Long pointKey);

    PointHistory save(PointHistory pointHistory);
}
