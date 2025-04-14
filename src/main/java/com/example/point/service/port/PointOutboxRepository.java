package com.example.point.service.port;

import com.example.point.domain.model.PointOutbox;

public interface PointOutboxRepository {

    void save(PointOutbox pointOutbox);

    void deleteAll();
}
